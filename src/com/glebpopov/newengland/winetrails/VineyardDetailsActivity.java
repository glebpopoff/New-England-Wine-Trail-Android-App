package com.glebpopov.newengland.winetrails;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.glebpopov.newengland.winetrails.db.VineyardsDbAdapter;
import com.glebpopov.newengland.winetrails.feeds.FacebookFeedActivity;
import com.glebpopov.newengland.winetrails.feeds.TwitterFeedActivity;
import com.glebpopov.newengland.winetrails.location.LocationUtils;
import com.glebpopov.newengland.winetrails.parser.JSONParser;

public class VineyardDetailsActivity extends MainActivity {
	private TabHost mTabHost;
	private String mFid;
	private String mTname;
	private VineyardsDbAdapter mDbHelper;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vineyard_details);

		initTopMenu(new Integer[] { R.id.home_btn, R.id.back_btn });
		initBottomMenu(false);
		
		mDbHelper = new VineyardsDbAdapter(this);
		mDbHelper.open();
		
		String id = getIntent().getExtras().getString("id");
		final HashMap<String, String> vineyard = mDbHelper.getRecord(Long.parseLong(id));
		
		if (!vineyard.isEmpty()) {
			try {
				mTabHost = (TabHost) findViewById(android.R.id.tabhost);
				mTabHost.setup();
				setupTab(new TextView(this), "About", R.id.vineyard_details_info);
				setupTab(new TextView(this), "Notes", R.id.vineyard_details_actions);
				setupTab(new TextView(this), "Reviews", R.id.vineyard_details_reviews);
				mTabHost.setCurrentTab(0);

				// facebook button

				if (vineyard.get("facebook").length() > 0 && isNetworkAvailable()) {
					String fid = "";

					// getting facebook id
					Pattern p = Pattern.compile("-?\\d+");
					Matcher m = p.matcher(vineyard.get("facebook"));
					if (m.find())
						fid = m.group();
					else {
						String[] fparts = vineyard.get("facebook").split("/");
						if (fparts.length == 4) {
							try {
								JSONObject finfo = JSONParser
										.getJSONObjectfromURL("https://graph.facebook.com/" + fparts[3]);
								fid = finfo.getString("id");
							} catch (Exception e) {
								Log.e(VineyardDetailsActivity.class.toString(),
										"Error getting fid: " + e.toString());
							}
						}
					}

					if (fid.length() > 0) {
						mFid = fid;
						TextView socialsText = (TextView) findViewById(R.id.vd_socials_check);
						socialsText.setVisibility(0);
						new FbFeedReceiver().execute();
					}
				}

				// twitter button

				if (vineyard.get("twitter").length() > 0 && isNetworkAvailable()) {
					String tName = "";
					String[] tNameParts = vineyard.get("twitter").split("/");
					tName = tNameParts[tNameParts.length - 1];

					if (tName.length() > 0) {
						mTname = tName;
						TextView socialsText = (TextView) findViewById(R.id.vd_socials_check);
						socialsText.setVisibility(0);
						new TwFeedReceiver().execute();
					}
				}

				// info tab

				TextView vname = (TextView) findViewById(R.id.vd_name);
				vname.setText(vineyard.get("name"));

				TextView vaddress = (TextView) findViewById(R.id.vd_address);
				vaddress.setText(vineyard.get("address"));

				TextView vdistance = (TextView) findViewById(R.id.vd_distance);
				vdistance.setText(LocationUtils.distanceToStr(vineyard.get("distance")));

				TextView vphone = (TextView) findViewById(R.id.vd_phone);
				vphone.setText(vineyard.get("phone"));

				TextView vurl = (TextView) findViewById(R.id.vd_url);
				vurl.setText(vineyard.get("url").length() > 0 ? vineyard.get("url") : "none");

				final Button showMapButton = (Button) findViewById(R.id.vd_show_map);
				showMapButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						makeExternalIntent(VineyardDetailsActivity.this, "geo", vineyard.get("lat") + ","
								+ vineyard.get("lon"));
					}
				});

				final Button callButton = (Button) findViewById(R.id.vd_call);
				callButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						makeExternalIntent(VineyardDetailsActivity.this, "tel", vineyard.get("phone"));
					}
				});

				final Button visitButton = (Button) findViewById(R.id.vd_visit);
				visitButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						makeExternalIntent(VineyardDetailsActivity.this, "url", vineyard.get("url"));
					}
				});
				
				LinearLayout shareButton = (LinearLayout) findViewById(R.id.vd_share);
				shareButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						String notes = "";
						Cursor notesCursor = mDbHelper.fetchRecord(Long.parseLong(vineyard.get("_id")));
						if (notesCursor.getCount() > 0) {
							notes = notesCursor.getString(notesCursor.getColumnIndexOrThrow(VineyardsDbAdapter.KEY_NOTES));
						}
						notesCursor.close();
						
						final Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("text/plain");
						intent.putExtra(Intent.EXTRA_SUBJECT, vineyard.get("name"));
						intent.putExtra(Intent.EXTRA_TEXT, notes);
						try {
							startActivity(Intent.createChooser(intent, "Share via"));
						} catch (Exception e) {
							Log.e(VineyardNotesActivity.class.toString(), "Error sharing notes: " + e.toString());
						}
					}
				});
				
				// actions tab
				
				LinearLayout notesButton = (LinearLayout) findViewById(R.id.vd_notes);
				notesButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent notesIntent = new Intent(v.getContext(), VineyardNotesActivity.class);
						notesIntent.putExtra("id", vineyard.get("_id"));
						startActivityForResult(notesIntent, 0);
					}
				});
				
				final LinearLayout addToItineraryButton = (LinearLayout) findViewById(R.id.vd_add_to_itinerary);
				final LinearLayout removeFromItineraryButton = (LinearLayout) findViewById(R.id.vd_remove_from_itinerary);
				
				if (vineyard.get("in_itinerary").equalsIgnoreCase("1")) {
					addToItineraryButton.setVisibility(View.GONE);
					removeFromItineraryButton.setVisibility(View.VISIBLE);
				}
				
				addToItineraryButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						vineyard.put("in_itinerary", "1");
						if (mDbHelper.updateRecord(vineyard)){
							addToItineraryButton.setVisibility(View.GONE);
							removeFromItineraryButton.setVisibility(View.VISIBLE);
						}
						else Toast.makeText(VineyardDetailsActivity.this, "Error adding vineyard to itinerary!", Toast.LENGTH_SHORT).show();
					}
				});
				
				removeFromItineraryButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						vineyard.put("in_itinerary", "0");
						if (mDbHelper.updateRecord(vineyard)){
							addToItineraryButton.setVisibility(View.VISIBLE);
							removeFromItineraryButton.setVisibility(View.GONE);
						}
						else Toast.makeText(VineyardDetailsActivity.this, "Error removing vineyard from itinerary!", Toast.LENGTH_SHORT).show();
					}
				});
				
				LinearLayout viewItineraryButton = (LinearLayout) findViewById(R.id.vd_view_itinerary);
				viewItineraryButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent itineraryIntent = new Intent(v.getContext(), VineyardsItineraryActivity.class);
						startActivityForResult(itineraryIntent, 0);
					}
				});

				// reviews tab
				
				if(isNetworkAvailable()) {
					WebView reviewsWebView = (WebView) findViewById(R.id.vineyard_details_reviews);
					reviewsWebView.getSettings().setJavaScriptEnabled(true);
					reviewsWebView.getSettings().setDomStorageEnabled(true);
					reviewsWebView.setWebViewClient(new WebViewClient());
					reviewsWebView.loadUrl(vineyard.get("gplace_url"));
				}

			} catch (Exception e) {
				Log.e(VineyardDetailsActivity.class.toString(), "Error loading data: " + e.toString());
			}

		} else {
			makeInternalIntent(this, "back", "Error loading data");
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}

	private void setupTab(final View view, final String tag, int id) {
		View tabview = createTabView(mTabHost.getContext(), tag);
		TabSpec spec = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(id);
		mTabHost.addTab(spec);
	}

	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}

	private class FbFeedReceiver extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			JSONObject fbFeed = JSONParser
					.getJSONObjectfromURL("http://www.facebook.com/feeds/page.php?format=json&id=" + mFid);
			try {
				if (fbFeed.get("link") != JSONObject.NULL) {
					return true;
				}
			} catch (JSONException e) {
				Log.e(VineyardDetailsActivity.class.toString(),
						"Error parsing facebook feed: " + e.toString());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			TextView socialsText = (TextView) findViewById(R.id.vd_socials_check);
			socialsText.setVisibility(View.GONE);
			if (result == true) {
				LinearLayout socialsCont = (LinearLayout) findViewById(R.id.vd_socials);
				socialsCont.setVisibility(0);
				final Button facebookButton = (Button) findViewById(R.id.vd_facebook);
				facebookButton.setVisibility(0);
				facebookButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent showFeedIntent = new Intent(v.getContext(), FacebookFeedActivity.class);
						showFeedIntent.putExtra("fid", mFid);
						startActivityForResult(showFeedIntent, 0);
					}
				});
			}
		}
	}

	private class TwFeedReceiver extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			JSONArray feedEntries = JSONParser
					.getJSONArrayfromURL("https://twitter.com/statuses/user_timeline/" + mTname + ".json");
			try {
				if (feedEntries != null) {
					return true;
				}
			} catch (Exception e) {
				Log.e(VineyardDetailsActivity.class.toString(), "Error parsing twitter feed: " + e.toString());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			TextView socialsText = (TextView) findViewById(R.id.vd_socials_check);
			socialsText.setVisibility(View.GONE);
			if (result == true) {
				LinearLayout socialsCont = (LinearLayout) findViewById(R.id.vd_socials);
				socialsCont.setVisibility(0);
				final Button twitterButton = (Button) findViewById(R.id.vd_twitter);
				twitterButton.setVisibility(0);
				twitterButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent showFeedIntent = new Intent(v.getContext(), TwitterFeedActivity.class);
						showFeedIntent.putExtra("tname", mTname);
						startActivityForResult(showFeedIntent, 0);
					}
				});
			}
		}

	}

}
