package com.glebpopov.newengland.winetrails.location;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

public class LocationUtils {
	
	public static Double getDistanceTo(Location loc, String lat, String lon){
		double distance = 100000;
		double mile = 1609.344;
		if(lat.length() > 0 && lon.length() > 0) {
			Location location = new Location("");  
			location.setLatitude(Double.parseDouble(lat));  
			location.setLongitude(Double.parseDouble(lon));
			
			distance = (double) (loc.distanceTo(location) / mile);
		}
		return distance;
	}
	
	public static String distanceToStr (String dist){
		String distanceStr = "cannot resolve";
		if (!dist.equalsIgnoreCase("100000")) {
			try {
				Double distance = Double.parseDouble(dist);
				if(distance > 0 && distance < 1) distanceStr = "less than a mile";
				else distanceStr =  distance.intValue() + " miles away";
			} catch (Exception e) {
				Log.e(LocationUtils.class.toString(), "Error parsing distance: " + e.toString());
			}
		}
		return distanceStr;
	}
	
	public static String[] getCoordinates (Context context, String address){
		String[] result = {"", ""};
		Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocationName(address, 5);
            if (addresses.size() > 0) {
            	result[0] = addresses.get(0).getLatitude() + "";
            	result[1] = addresses.get(0).getLongitude() + "";
            }    
        } catch (Exception e) {
        	Log.e(LocationUtils.class.toString(), "Error getting coordinates: " + e.toString());
        }
        return result;
	}
	
}
