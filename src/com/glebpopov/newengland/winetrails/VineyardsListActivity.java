package com.glebpopov.newengland.winetrails;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.glebpopov.newengland.winetrails.adapters.VineyardsListItemAdapter;
import com.glebpopov.newengland.winetrails.db.VineyardsDbAdapter;
import com.glebpopov.newengland.winetrails.location.LocationHandler;
import com.glebpopov.newengland.winetrails.location.LocationUtils;
import com.glebpopov.newengland.winetrails.parser.JSONParser;

public class VineyardsListActivity extends MainActivity {
	private ArrayList<HashMap<String, String>> mVineyardsData;
	private String mState;
	private String mUrl;
	private VineyardsDbAdapter mDbHelper;
	private String jsonData = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vineyards_list);

		initTopMenu(new Integer[] { R.id.home_btn, R.id.back_btn, R.id.refresh_btn, R.id.search_btn });
		initBottomMenu(false);
		
		mDbHelper = new VineyardsDbAdapter(this);
		mDbHelper.open();

		try {
			Integer position = getIntent().getExtras().getInt("position");
			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> vineyards = (ArrayList<HashMap<String, String>>) getLastNonConfigurationInstance();

			if (vineyards == null) {
				mState = (String) getResources().getStringArray(R.array.states_names)[position];
				mVineyardsData = mDbHelper.getRecords(VineyardsDbAdapter.KEY_STATE + "=?", new String[] { mState });
				if(mVineyardsData.isEmpty()){
					mUrl = (String) getResources().getStringArray(R.array.urls)[position];
					Log.d(this.toString(), "Resource URL: " + mUrl);
					int resourceID = getResources().getIdentifier(mUrl , "raw", getPackageName());
					try
					{
						InputStream inp = this.getResources().openRawResource(resourceID); 
						jsonData = convertStreamToString(inp);
					} catch (Exception ex)
					{
						Log.e(this.toString(), "Unable to parse/read JSON data: " + ex);
					}
					if (jsonData.length() > 0) new VineyardsInfoReceiver().execute();
					else makeInternalIntent(this, "back", "No info for this state");
				}
				else populateVineyardsList(mVineyardsData);
			} else {
				mVineyardsData = vineyards;
				populateVineyardsList(mVineyardsData);
			}
		} catch (Exception e) {
			Log.e(VineyardsListActivity.class.toString(), "Error getting data: " + e.toString());
			makeInternalIntent(this, "back", "No info for this state");
		}
	}
	
	private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
        	Log.e(VineyardsListActivity.class.toString(), "Unable to parse/read JSON data: " + e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            	Log.e(VineyardsListActivity.class.toString(), "Unable to parse/read JSON data: " + e);
            }
        }
        return sb.toString();
    }

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mVineyardsData;
	}
	
	@Override
	public void refreshActivity() {
		for (int i = 0; i < mVineyardsData.size(); i++) {
			String distance = "100000";
			if (mVineyardsData.get(i).containsKey("lat") && mVineyardsData.get(i).containsKey("lon")) {
				try {
					LocationHandler locHandler = new LocationHandler(VineyardsListActivity.this);
					Location loc = locHandler.getLocation();
					distance = LocationUtils.getDistanceTo(loc, mVineyardsData.get(i).get("lat"), mVineyardsData.get(i).get("lon")).toString();
				} catch (Exception e) {
					Log.e(VineyardsListActivity.class.toString(),
							"Error getting distance: " + e.toString());
				}
			}
			mVineyardsData.get(i).put("distance", distance);
		}
		if (!mDbHelper.updateRecords(mVineyardsData)) Log.e(VineyardsListActivity.class.toString(), "Error updating records");
		populateVineyardsList(mVineyardsData);
		/*
		if(mDbHelper.deleteRecords(VineyardsDbAdapter.KEY_STATE + "=?", new String[] { mState })){
			Intent startIntent = getIntent();
			startActivity(startIntent);
			finish();
		}
		*/
	}

	private class VineyardsInfoReceiver extends AsyncTask<Void, String, Void> {
		private ProgressDialog progressDialog;

		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(VineyardsListActivity.this, "Downloading",
					"Please wait...", true);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			
			JSONArray vineyards = null;
			try {
				
				vineyards = new JSONArray(jsonData);
				//vineyards = JSONParser.getJSONArrayfromURL(mUrl);
			} catch (JSONException e1) {
				Log.e(this.toString(), "Unable to parse JSON: " + e1);
			}
			if (vineyards == null) {
				publishProgress("Unable to retrieve data");
			} else {
				try {
					ArrayList<HashMap<String, String>> records = new ArrayList<HashMap<String, String>>();
					for (int i = 0; i < vineyards.length(); i++) {
						HashMap<String, String> map = new HashMap<String, String>();
						JSONObject obj = vineyards.getJSONObject(i);
						JSONArray objFieldNames = obj.names();
						for (int j = 0; j < objFieldNames.length(); j++) {
							String fieldName = objFieldNames.getString(j);
							if (obj.has(fieldName))
								map.put(fieldName, obj.getString(fieldName));
						}
						map.put("state", mState);
						// distance
						String distance = "100000";
						if (obj.has("lat") && obj.has("lon")) {
							if(obj.getString("lat").length() == 0 || obj.getString("lon").length() == 0){
								String[] coords = LocationUtils.getCoordinates(getApplicationContext(), obj.getString("address"));
								map.put("lat", coords[0]); map.put("lon", coords[1]);
							}
							try {
								LocationHandler locHandler = new LocationHandler(VineyardsListActivity.this);
								Location loc = locHandler.getLocation();
								distance = LocationUtils.getDistanceTo(loc, map.get("lat"), map.get("lon")).toString();
							} catch (Exception e) {
								Log.e(VineyardsListActivity.class.toString(),
										"Error getting distance: " + e.toString());
							}
						}
						map.put("distance", distance);
						records.add(map);
					}
					if (mDbHelper.createRecords(records)) mVineyardsData = mDbHelper.getRecords(VineyardsDbAdapter.KEY_STATE + "=?", new String[] { mState });
					else Log.e(VineyardsListActivity.class.toString(), "Error inserting records");
				} catch (JSONException e) {
					Log.e(VineyardsListActivity.class.toString(), "Error parsing data: " + e.toString());
				}
			}
			return null;
		}

		protected void onProgressUpdate(String... str) {
			makeInternalIntent(VineyardsListActivity.this, "back", str[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			populateVineyardsList(mVineyardsData);
		}
	}

	private void populateVineyardsList(final ArrayList<HashMap<String, String>> vineyardsData) {
		try {
			Collections.sort(vineyardsData, new Comparator<HashMap<String, String>>() {
				@Override
				public int compare(HashMap<String, String> arg0, HashMap<String, String> arg1) {
					Double key0 = Double.parseDouble(arg0.get("distance"));
					Double key1 = Double.parseDouble(arg1.get("distance"));
					return key0.compareTo(key1);
				}
			});
		} catch (Exception e) {
			Log.e(VineyardsListActivity.class.toString(), "Error sorting data: " + e.toString());
		}
		ListView lv = (ListView) findViewById(R.id.vl_listview);
		lv.setAdapter(new VineyardsListItemAdapter(this, vineyardsData, mDbHelper));
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);
		lv.setItemsCanFocus(true);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent showDetailsIntent = new Intent(view.getContext(), VineyardDetailsActivity.class);
				showDetailsIntent.putExtra("id", vineyardsData.get(position).get("_id"));
				startActivityForResult(showDetailsIntent, 0);
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}

	@Override
	public boolean onSearchRequested() {
		searchList();
		return false;
	}

	@Override
	public void searchList() {
		final EditText searchBox = (EditText) findViewById(R.id.search_box);
		searchBox.setVisibility(0);
		searchBox.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<HashMap<String, String>> dataSorted = new ArrayList<HashMap<String, String>>();
				Integer textlength = searchBox.getText().length();
				dataSorted.clear();

				for (int i = 0; i < mVineyardsData.size(); i++) {
					if (textlength <= mVineyardsData.get(i).get("name").length()) {
						if (mVineyardsData.get(i).get("name").toLowerCase()
								.indexOf(searchBox.getText().toString().toLowerCase()) >= 0) {
							dataSorted.add(mVineyardsData.get(i));
						}
					}
				}
				populateVineyardsList(dataSorted);
			}
		});
	}
}
