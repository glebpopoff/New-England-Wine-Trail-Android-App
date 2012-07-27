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
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.glebpopov.newengland.winetrails.MainActivity;
import com.glebpopov.newengland.winetrails.R;
import com.glebpopov.newengland.winetrails.parser.JSONParser;

public class TwitterFeedActivity extends MainActivity {
	private ArrayList<HashMap<String, String>> mFeedData;
	private String mUrl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_feed_list);
		
		TextView menuText = (TextView) findViewById(R.id.menu_text);
		menuText.setText("Twitter Feed");

		initTopMenu(new Integer[] { R.id.home_btn, R.id.back_btn, R.id.refresh_btn, R.id.search_btn });

		try {
			String tName = getIntent().getExtras().getString("tname");
			mUrl = "https://twitter.com/statuses/user_timeline/" + tName + ".json";
			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> feedData = (ArrayList<HashMap<String, String>>) getLastNonConfigurationInstance();

			if (feedData == null) {
				mFeedData = new ArrayList<HashMap<String, String>>();
				new FeedReceiver().execute();
			} else {
				mFeedData = feedData;
				populateFeedList(mFeedData);
			}
		} catch (Exception e) {
			Log.e(TwitterFeedActivity.class.toString(), "Error getting data: " + e.toString());
			makeInternalIntent(this, "back", "No twitter info");
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mFeedData;
	}

	private class FeedReceiver extends AsyncTask<Void, String, Void> {
		private ProgressDialog progressDialog;

		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(TwitterFeedActivity.this, "Loading", "Please wait...", true);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			JSONArray feedEntries = JSONParser.getJSONArrayfromURL(mUrl);
			try {
				if (feedEntries == null) {
					publishProgress("Twitter feed not found");
				} else {
					for (int i = 0; i < feedEntries.length(); i++) {
						HashMap<String, String> feedMap = new HashMap<String, String>();
						JSONObject obj = feedEntries.getJSONObject(i);
						JSONArray objFieldNames = obj.names();
						for (int j = 0; j < objFieldNames.length(); j++) {
							String fieldName = objFieldNames.getString(j);
							if (obj.has(fieldName))
								feedMap.put(fieldName, obj.getString(fieldName));
						}
						feedMap.put("id", String.valueOf(i));
						if (obj.has("created_at")) {
							try {
								SimpleDateFormat curFormater = new SimpleDateFormat(
										"EEE MMM dd HH:mm:ss Z yyyy");
								SimpleDateFormat postFormater = new SimpleDateFormat(
										"MMMM dd, yyyy 'at' h:mm a");
								Date dateObj = curFormater.parse(obj.getString("created_at"));
								String pubDate = postFormater.format(dateObj);
								feedMap.put("date", pubDate);
							} catch (java.text.ParseException e) {
								Log.e(TwitterFeedActivity.class.toString(),
										"Error parsing date: " + e.toString());
							}
						}
						mFeedData.add(feedMap);
					}
				}
			} catch (JSONException e) {
				Log.e(TwitterFeedActivity.class.toString(), "Error parsing twitter feed: " + e.toString());
			}
			return null;
		}

		protected void onProgressUpdate(String... str) {
			makeInternalIntent(TwitterFeedActivity.this, "back", str[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			populateFeedList(mFeedData);
		}
	}

	private void populateFeedList(ArrayList<HashMap<String, String>> feedData) {
		ListAdapter adapter = new SimpleAdapter(this, feedData, R.layout.twitter_feed_list_item,
				new String[] { "date", "text" }, new int[] { R.id.tfl_date, R.id.tfl_text });

		final ListView lv = (ListView) findViewById(R.id.tfl_list);
		lv.setAdapter(adapter);
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
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<HashMap<String, String>> feedDataSorted = new ArrayList<HashMap<String, String>>();
				Integer textlength = searchBox.getText().length();
				feedDataSorted.clear();

				for (int i = 0; i < mFeedData.size(); i++) {
					if (textlength <= mFeedData.get(i).get("text").length()) {
						if (mFeedData.get(i).get("text").toLowerCase()
								.indexOf(searchBox.getText().toString().toLowerCase()) >= 0) {
							feedDataSorted.add(mFeedData.get(i));
						}
					}
				}
				populateFeedList(feedDataSorted);
			}
		});
	}
}
