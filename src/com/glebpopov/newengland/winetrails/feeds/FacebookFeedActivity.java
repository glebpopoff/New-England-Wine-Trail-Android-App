package com.glebpopov.newengland.winetrails.feeds;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.glebpopov.newengland.winetrails.MainActivity;
import com.glebpopov.newengland.winetrails.R;
import com.glebpopov.newengland.winetrails.adapters.FacebookFeedItemAdapter;
import com.glebpopov.newengland.winetrails.parser.JSONParser;

public class FacebookFeedActivity extends MainActivity {
	private ArrayList<HashMap<String, String>> mFeedData;
	private String mUrl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_feed_list);

		TextView menuText = (TextView) findViewById(R.id.menu_text);
		menuText.setText("Facebook Feed");
		menuText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);

		initTopMenu(new Integer[] { R.id.home_btn, R.id.back_btn, R.id.refresh_btn, R.id.search_btn });

		try {
			String fid = getIntent().getExtras().getString("fid");
			mUrl = "http://www.facebook.com/feeds/page.php?format=json&id=" + fid;
			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> feedData = (ArrayList<HashMap<String, String>>) getLastNonConfigurationInstance();

			if (feedData == null) {
				mFeedData = new ArrayList<HashMap<String, String>>();
				new FacebookFeedReceiver().execute();
			} else {
				mFeedData = feedData;
				populateFacebookFeedList(mFeedData);
			}
		} catch (Exception e) {
			Log.e(FacebookFeedActivity.class.toString(), "Error getting data: " + e.toString());
			makeInternalIntent(this, "back", "No facebook info");
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mFeedData;
	}

	private class FacebookFeedReceiver extends AsyncTask<Void, String, Void> {
		private ProgressDialog progressDialog;

		protected void onPreExecute() {
			progressDialog = ProgressDialog
					.show(FacebookFeedActivity.this, "Loading", "Please wait...", true);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			JSONObject fbFeed = JSONParser.getJSONObjectfromURL(mUrl);
			try {
				if (fbFeed.get("link") == JSONObject.NULL) {
					publishProgress("Facebook feed not found");
				} else {
					JSONArray fbFeedEntries = fbFeed.getJSONArray("entries");
					for (int i = 0; i < fbFeedEntries.length(); i++) {
						HashMap<String, String> fbMap = new HashMap<String, String>();
						JSONObject obj = fbFeedEntries.getJSONObject(i);
						JSONArray objFieldNames = obj.names();
						for (int j = 0; j < objFieldNames.length(); j++) {
							String fieldName = objFieldNames.getString(j);
							if (obj.has(fieldName))
								fbMap.put(fieldName, obj.getString(fieldName));
						}
						fbMap.put("id", String.valueOf(i));
						if (obj.has("published")) {
							try {
								SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
								SimpleDateFormat postFormater = new SimpleDateFormat(
										"MMMM dd, yyyy 'at' h:mm a");
								Date dateObj = curFormater.parse(obj.getString("published"));
								String pubDate = postFormater.format(dateObj);
								fbMap.put("date", pubDate);
							} catch (java.text.ParseException e) {
								Log.e(FacebookFeedActivity.class.toString(),
										"Error parsing date: " + e.toString());
							}
						}
						mFeedData.add(fbMap);
					}
				}
			} catch (JSONException e) {
				Log.e(FacebookFeedActivity.class.toString(), "Error parsing facebook feed: " + e.toString());
			}
			return null;
		}

		protected void onProgressUpdate(String... str) {
			makeInternalIntent(FacebookFeedActivity.this, "back", str[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			populateFacebookFeedList(mFeedData);
		}
	}

	private void populateFacebookFeedList(ArrayList<HashMap<String, String>> feedData) {
		ListView lv = (ListView) findViewById(R.id.vdf_list);
		lv.setAdapter(new FacebookFeedItemAdapter(this, feedData));
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);
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
				ArrayList<HashMap<String, String>> feedDataSorted = new ArrayList<HashMap<String, String>>();
				Integer textlength = searchBox.getText().length();
				feedDataSorted.clear();

				for (int i = 0; i < mFeedData.size(); i++) {
					if (textlength <= mFeedData.get(i).get("content").length()) {
						if (mFeedData.get(i).get("content").toLowerCase()
								.indexOf(searchBox.getText().toString().toLowerCase()) >= 0) {
							feedDataSorted.add(mFeedData.get(i));
						}
					}
				}
				populateFacebookFeedList(feedDataSorted);
			}
		});
	}
}
