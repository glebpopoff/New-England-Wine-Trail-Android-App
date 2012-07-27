package com.glebpopov.newengland.winetrails.map;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.glebpopov.newengland.winetrails.R;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

@SuppressWarnings("rawtypes")
public class ItineraryMapItemizedOverlay extends ItemizedOverlay {
	private Context mContext;
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	public ItineraryMapItemizedOverlay(Drawable defaultMarker, Context context) {
		//super(defaultMarker);
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}
	
	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}

	@Override
	public int size() {
	  return mOverlays.size();
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
	  View view = LayoutInflater.from(mContext).inflate(R.layout.itinerary_map_toast, null);

	  TextView name = (TextView) view.findViewById(R.id.im_name);
	  name.setText(item.getTitle());
	  TextView address = (TextView) view.findViewById(R.id.im_address);
	  address.setText(item.getSnippet());
	  //address.setText(item.routableAddress());

	  Toast toast = new Toast(mContext);
	  toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
	  toast.setDuration(Toast.LENGTH_SHORT);
	  toast.setView(view);
	  toast.show();
	  
	  return true;
	}

}
