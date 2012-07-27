package com.glebpopov.newengland.winetrails.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.glebpopov.newengland.winetrails.R;
import com.glebpopov.newengland.winetrails.db.VineyardsDbAdapter;

public class VineyardsItineraryItemAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<HashMap<String, String>> mVineyards;
	private VineyardsDbAdapter mDbHelper;

	private static class ViewHolder {
		TextView mName;
		TextView mAddress;
		TextView mPhone;
		Button mRemoveBtn;
	}

	public VineyardsItineraryItemAdapter(Context c, ArrayList<HashMap<String, String>> vineyards, VineyardsDbAdapter dbHelper) {
		mContext = c;
		mVineyards = vineyards;
		mDbHelper = dbHelper;
	}

	public int getCount() {
		return mVineyards.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final HashMap<String, String> vineyard = (HashMap<String, String>) mVineyards.get(position);

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.vineyards_itinerary_list_item, null);

			holder = new ViewHolder();
			holder.mName = (TextView) convertView.findViewById(R.id.vili_name);
			holder.mAddress = (TextView) convertView.findViewById(R.id.vili_address);
			holder.mPhone = (TextView) convertView.findViewById(R.id.vili_phone);
			holder.mRemoveBtn = (Button) convertView.findViewById(R.id.vili_remove);

			holder.mRemoveBtn.setFocusable(false);
			holder.mRemoveBtn.setFocusableInTouchMode(false);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.mName.setText(vineyard.get("name"));
		holder.mAddress.setText(vineyard.get("address"));
		holder.mPhone.setText(vineyard.get("phone"));

		holder.mRemoveBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				vineyard.put("in_itinerary", "0");
				mDbHelper.updateRecord(vineyard);
				mVineyards.remove(position);
				VineyardsItineraryItemAdapter.this.notifyDataSetChanged();
			}
		});

		return convertView;
	}

}
