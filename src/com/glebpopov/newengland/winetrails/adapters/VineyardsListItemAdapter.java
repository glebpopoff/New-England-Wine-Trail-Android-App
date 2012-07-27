package com.glebpopov.newengland.winetrails.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.glebpopov.newengland.winetrails.MainActivity;
import com.glebpopov.newengland.winetrails.R;
import com.glebpopov.newengland.winetrails.db.VineyardsDbAdapter;
import com.glebpopov.newengland.winetrails.location.LocationUtils;

public class VineyardsListItemAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<HashMap<String, String>> mVineyards;
	private VineyardsDbAdapter mDbHelper;

	private static class ViewHolder {
		TextView mName;
		TextView mAddress;
		TextView mPhone;
		TextView mDistance;
		Button mShowMap;
		Button mCall;
		Button mAdd;
		RatingBar mRating;
		LinearLayout mRate;
	}

	public VineyardsListItemAdapter(Context c, ArrayList<HashMap<String, String>> vineyards,
			VineyardsDbAdapter dbHelper) {
		mContext = c;
		mVineyards = vineyards;
		mDbHelper = dbHelper;
	}

	public int getCount() {
		return mVineyards.size();
	}

	public Object getItem(int position) {
		return mVineyards.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final HashMap<String, String> vineyard = (HashMap<String, String>) mVineyards.get(position);

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.vineyards_list_item, null);

			holder = new ViewHolder();
			holder.mName = (TextView) convertView.findViewById(R.id.li_name);
			holder.mAddress = (TextView) convertView.findViewById(R.id.li_address);
			holder.mPhone = (TextView) convertView.findViewById(R.id.li_phone);
			holder.mDistance = (TextView) convertView.findViewById(R.id.li_distance);
			holder.mShowMap = (Button) convertView.findViewById(R.id.li_show_map);
			holder.mCall = (Button) convertView.findViewById(R.id.li_call);
			holder.mAdd = (Button) convertView.findViewById(R.id.li_add_to_itinerary);
			holder.mRate = (LinearLayout) convertView.findViewById(R.id.li_rate);
			holder.mRating = (RatingBar) convertView.findViewById(R.id.li_rating);

			holder.mShowMap.setFocusable(false);
			holder.mShowMap.setFocusableInTouchMode(false);

			holder.mCall.setFocusable(false);
			holder.mCall.setFocusableInTouchMode(false);
			
			holder.mAdd.setFocusable(false);
			holder.mAdd.setFocusableInTouchMode(false);

			holder.mRate.setFocusable(false);
			holder.mRate.setFocusableInTouchMode(false);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.mName.setText(vineyard.get("name"));
		holder.mAddress.setText(vineyard.get("address"));
		holder.mPhone.setText(vineyard.get("phone"));
		holder.mDistance.setText(LocationUtils.distanceToStr(vineyard.get("distance")));
		holder.mRating.setRating(Float.parseFloat(vineyard.get("rating")));

		holder.mShowMap.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.makeExternalIntent(mContext, "geo",
						vineyard.get("lat") + "," + vineyard.get("lon"));
			}
		});

		holder.mCall.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.makeExternalIntent(mContext, "tel", vineyard.get("phone"));
			}
		});
		
		holder.mAdd.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				vineyard.put("in_itinerary", "1");
				if (mDbHelper.updateRecord(vineyard)){
					Toast.makeText(mContext, "Successfully added " + vineyard.get("name")
							+ " to your itinerary", Toast.LENGTH_SHORT).show();
				}
			}
		});

		holder.mRate.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				final RatingBar rb = new RatingBar(mContext);
				rb.setNumStars(5);
				rb.setStepSize(1);
				final Cursor cursor = mDbHelper.fetchRecord(Long.parseLong(vineyard.get("_id")));
				if (cursor.getCount() > 0) {
					String ratingStr = cursor.getString(cursor.getColumnIndexOrThrow(VineyardsDbAdapter.KEY_RATING));
					rb.setRating(Float.parseFloat(ratingStr));
				}

				new AlertDialog.Builder(mContext).setTitle("Rate this vineyard!").setView(rb)
						.setNegativeButton("Cancel", null)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								vineyard.put("rating", rb.getRating() + "");
								mDbHelper.updateRecord(vineyard);
								RatingBar ratingBar = (RatingBar) v.findViewById(R.id.li_rating);
								ratingBar.setRating(rb.getRating());
								dialog.dismiss();
							}
						}).show();
			}
		});

		return convertView;
	}

}
