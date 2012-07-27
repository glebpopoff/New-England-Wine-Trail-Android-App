package com.glebpopov.newengland.winetrails.adapters;

import com.glebpopov.newengland.winetrails.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StateItemAdapter extends BaseAdapter {
    private Context mContext;
    
    private String[] mStatesNames;
    private TypedArray mStatesImages;
    
    private static class ViewHolder {
		TextView mName;
		ImageView mImage;
	}
    
    public StateItemAdapter(Context c) {
        mContext = c;
        mStatesNames = mContext.getResources().getStringArray(R.array.states_names);
        mStatesImages = mContext.getResources().obtainTypedArray(R.array.states_images);
    }

    public int getCount() {
        return mStatesNames.length;
    }
    
    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	ViewHolder holder;
    	
		if(convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.state_item, null);
			
			holder = new ViewHolder();
			holder.mName = (TextView) convertView.findViewById(R.id.name);
			holder.mImage = (ImageView) convertView.findViewById(R.id.image);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.mName.setText(mStatesNames[position]);
		holder.mImage.setBackgroundDrawable(mStatesImages.getDrawable(position));
		
		mStatesImages.recycle();
		
		return convertView;
	}
    
}
