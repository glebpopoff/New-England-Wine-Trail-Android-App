package com.glebpopov.newengland.winetrails;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.back:
			makeInternalIntent(this, "back", "");
			return true;
		case R.id.refresh:
			refreshActivity();
			return true;
		case R.id.about:
			new AlertDialog.Builder(this).setTitle("About").setMessage("New England Wine Trail Application")
					.setNeutralButton("Close", null).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				if (intent.hasExtra("data")) {
					String data = intent.getExtras().getString("data");
					if (data.length() > 0)
						Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public static void makeInternalIntent(Activity activity, String type, String data) {
		Intent intent = new Intent();
		if (type.equals("back")) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("data", data);
			activity.setResult(RESULT_OK, intent);
			activity.finish();
		}
	}

	public static void makeExternalIntent(Context context, String type, String data) {
		Intent intent = new Intent();
		if (data.length() > 0) {
			if (type.equals("geo")) {
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + data));
			} else if (type.equals("tel")) {
				intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data));
			} else if (type.equals("url")) {
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
			}

			if (intent != null)
				context.startActivity(intent);
		}

	}

	public void refreshActivity() {
		Intent startIntent = getIntent();
		if (!Intent.ACTION_MAIN.equals(startIntent.getAction())) {
			startActivity(startIntent);
			finish();
		}
	}

	public void searchList() {
	}

	public void initTopMenu(Integer buttons[]) {
		for (final Integer button : buttons) {
			final ImageView menuBtn = (ImageView) findViewById(button);
			if (menuBtn != null) {
				menuBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						switch (button) {
						case R.id.home_btn:
							Intent intent = new Intent(v.getContext(), HomeActivity.class);
							startActivity(intent);
							finish();
							break;
						case R.id.back_btn:
							makeInternalIntent(MainActivity.this, "back", "");
							break;
						case R.id.refresh_btn:
							refreshActivity();
							break;
						case R.id.search_btn:
							searchList();
							break;
						default:
							break;
						}
					}
				});
			}
		}
	}

	public void initBottomMenu(Boolean isItenerary) {
		Button vineyardBtn = (Button) findViewById(R.id.vineyard_btn);
		Button itineraryBtn = (Button) findViewById(R.id.itinerary_btn);
		if (isItenerary) {
			vineyardBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					makeInternalIntent(MainActivity.this, "back", "");
				}
			});
		} else {
			itineraryBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), VineyardsItineraryActivity.class);
					startActivityForResult(intent, 0);
				}
			});
		}
	}

}
