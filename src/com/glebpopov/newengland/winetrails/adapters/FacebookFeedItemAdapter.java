package com.glebpopov.newengland.winetrails.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.glebpopov.newengland.winetrails.R;

public class FacebookFeedItemAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<HashMap<String, String>> mEntries;

	private static class ViewHolder {
		//TextView mTitle;
		//TextView mAlternate;
		TextView mPublished;
		WebView mContent;
	}

	public FacebookFeedItemAdapter(Context c, ArrayList<HashMap<String, String>> entries) {
		mContext = c;
		mEntries = entries;
	}

	public int getCount() {
		return mEntries.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final HashMap<String, String> vineyard = (HashMap<String, String>) mEntries.get(position);

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.facebook_feed_list_item, null);

			holder = new ViewHolder();
			//holder.mTitle = (TextView) convertView.findViewById(R.id.ffl_title);
			//holder.mAlternate = (TextView) convertView.findViewById(R.id.ffl_alternate);
			holder.mPublished = (TextView) convertView.findViewById(R.id.ffl_published);
			holder.mContent = (WebView) convertView.findViewById(R.id.ffl_content);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//holder.mTitle.setText(Html.fromHtml(vineyard.get("title")));
		//holder.mAlternate.setText(vineyard.get("alternate"));
		holder.mPublished.setText(vineyard.get("date"));
		holder.mContent.loadData(vineyard.get("content"), "text/html", null);

		return convertView;
	}

}
