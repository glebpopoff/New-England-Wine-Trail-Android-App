package com.glebpopov.newengland.winetrails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.glebpopov.newengland.winetrails.db.VineyardsDbAdapter;
import com.glebpopov.newengland.winetrails.map.ItineraryMapItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class VineyardsItineraryMapActivity extends MapActivity {
	private VineyardsDbAdapter mDbHelper;
	private ArrayList<HashMap<String, String>> mVineyards;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vineyards_itinerary_map);

		TextView menuText = (TextView) findViewById(R.id.menu_text);
		menuText.setText("Itinerary Map");
		initMenus();

		mDbHelper = new VineyardsDbAdapter(this);
		mDbHelper.open();
		
		mVineyards = mDbHelper.getRecords(VineyardsDbAdapter.KEY_IN_ITINERARY + "=1", null);

		MapView mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setStreetView(true);

		if (mVineyards.size() > 0) {
			try {
				List<Overlay> mapOverlays = mapView.getOverlays();
				Drawable marker = this.getResources().getDrawable(R.drawable.ic_marker);
				ItineraryMapItemizedOverlay itemizedoverlay = new ItineraryMapItemizedOverlay(marker, this);

				for (int i = 0; i < mVineyards.size(); i++) {
					GeoPoint point = new GeoPoint(
							(int) (Double.parseDouble(mVineyards.get(i).get("lat")) * 1E6),
							(int) (Double.parseDouble(mVineyards.get(i).get("lon")) * 1E6));
					OverlayItem overlayitem = new OverlayItem(point, mVineyards.get(i).get("name"),
							mVineyards.get(i).get("address"));
					itemizedoverlay.addOverlay(overlayitem);
				}

				mapOverlays.add(itemizedoverlay);
				
				MapController mc = mapView.getController();
				GeoPoint p = new GeoPoint((int) (Double.parseDouble(mVineyards.get(0).get("lat")) * 1E6),
						(int) (Double.parseDouble(mVineyards.get(0).get("lon")) * 1E6));
				mc.animateTo(p);
				mc.setZoom(10);

				mapView.invalidate();
			} catch (Exception e) {
				Log.e(VineyardsItineraryMapActivity.class.toString(), "Error creating overlay: " + e.toString());
			}
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}

	public void initMenus() {
		ImageView homeBtn = (ImageView) findViewById(R.id.home_btn);
		homeBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), HomeActivity.class);
				startActivity(intent);
				finish();
			}
		});

		ImageView backBtn = (ImageView) findViewById(R.id.back_btn);
		backBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK, new Intent());
				finish();
			}
		});

		Button vineyardBtn = (Button) findViewById(R.id.vineyard_btn);
		vineyardBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), HomeActivity.class);
				startActivity(intent);
				finish();
			}
		});

		Button itineraryBtn = (Button) findViewById(R.id.itinerary_btn);
		itineraryBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), VineyardsItineraryActivity.class);
				startActivityForResult(intent, 0);
			}
		});
	}
}
