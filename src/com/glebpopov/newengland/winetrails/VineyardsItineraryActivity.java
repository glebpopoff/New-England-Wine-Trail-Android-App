package com.glebpopov.newengland.winetrails;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.glebpopov.newengland.winetrails.adapters.VineyardsItineraryItemAdapter;
import com.glebpopov.newengland.winetrails.db.VineyardsDbAdapter;

public class VineyardsItineraryActivity extends MainActivity {
	private VineyardsDbAdapter mDbHelper;
	private ArrayList<HashMap<String, String>> mVineyards;
	private ProgressDialog mProgressDialog;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vineyards_itinerary_list);

		mDbHelper = new VineyardsDbAdapter(this);
		mDbHelper.open();

		TextView menuText = (TextView) findViewById(R.id.menu_text);
		menuText.setText("Itinerary");

		initTopMenu(new Integer[] { R.id.home_btn, R.id.back_btn });
		initBottomMenu(true);

		Button itineraryBtn = (Button) findViewById(R.id.itinerary_btn);
		itineraryBtn.setText("Show on Map");
		itineraryBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mProgressDialog = ProgressDialog.show(VineyardsItineraryActivity.this, "Loading", "Please wait...", true);
				Intent intent = new Intent(v.getContext(), VineyardsItineraryMapActivity.class);
				startActivityForResult(intent, 0);
			}
		});

		mVineyards = mDbHelper.getRecords(VineyardsDbAdapter.KEY_IN_ITINERARY + "=1", null);
		populateList(mVineyards);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		try {
			mProgressDialog.dismiss();
		} catch (Exception e) {
			// ignore error
		}
		super.onSaveInstanceState(outState);
	}

	public void populateList(final ArrayList<HashMap<String, String>> vineyards) {
		ListView lv = (ListView) findViewById(R.id.vi_list);
		lv.setAdapter(new VineyardsItineraryItemAdapter(this, mVineyards, mDbHelper));
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);
		lv.setItemsCanFocus(true);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent showDetailsIntent = new Intent(view.getContext(), VineyardDetailsActivity.class);
				showDetailsIntent.putExtra("id", vineyards.get(position).get("_id"));
				startActivityForResult(showDetailsIntent, 0);
			}
		});
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

				for (int i = 0; i < mVineyards.size(); i++) {
					if (textlength <= mVineyards.get(i).get("name").length()) {
						if (mVineyards.get(i).get("name").toLowerCase()
								.indexOf(searchBox.getText().toString().toLowerCase()) >= 0) {
							dataSorted.add(mVineyards.get(i));
						}
					}
				}
				populateList(dataSorted);
			}
		});
	}
}
