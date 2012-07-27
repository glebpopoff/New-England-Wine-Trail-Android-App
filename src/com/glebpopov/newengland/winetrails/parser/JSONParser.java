package com.glebpopov.newengland.winetrails.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {

	public static JSONArray getJSONArrayfromURL(String url) {
		JSONArray jArray = null;
		// try parse the string to a JSON array
		try {
			jArray = new JSONArray(getJSONString(url));
		} catch (JSONException e) {
			Log.e(JSONParser.class.toString(), "Error parsing data: " + e.toString());
		}
		return jArray;
	}
	
	public static JSONObject getJSONObjectfromURL(String url) {
		JSONObject jObject = null;
		// try parse the string to a JSON object
		try {
			jObject = new JSONObject(getJSONString(url));
		} catch (JSONException e) {
			Log.e(JSONParser.class.toString(), "Error parsing data: " + e.toString());
		}
		return jObject;
	}

	private static String getJSONString(String url) {
		InputStream content = null;
		String result = "";

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				content = entity.getContent();
			} else {
				Log.e(JSONParser.class.toString(), "Error getting response");
			}
		} catch (Exception e) {
			Log.e(JSONParser.class.toString(), "Error in http connection: " + e.toString());
		}

		// convert response to string
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
			content.close();
			result = builder.toString();
		} catch (Exception e) {
			Log.e(JSONParser.class.toString(), "Error converting result: " + e.toString());
		}

		return result;
	}

}
