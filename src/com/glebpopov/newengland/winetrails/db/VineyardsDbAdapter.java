package com.glebpopov.newengland.winetrails.db;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VineyardsDbAdapter {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_STATE = "state";
	public static final String KEY_NAME = "name";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_PHONE = "phone";
	public static final String KEY_URL = "url";
	public static final String KEY_TWITTER = "twitter";
	public static final String KEY_FACEBOOK = "facebook";
	public static final String KEY_GPLACE_URL = "gplace_url";
	public static final String KEY_LAT = "lat";
	public static final String KEY_LON = "lon";
	public static final String KEY_DISTANCE = "distance";
	public static final String KEY_IN_ITINERARY = "in_itinerary";
	public static final String KEY_NOTES = "notes";
	public static final String KEY_RATING = "rating";

	private String[] allColumns = { KEY_ROWID, KEY_STATE, KEY_NAME, KEY_ADDRESS, KEY_PHONE, KEY_URL,
			KEY_TWITTER, KEY_FACEBOOK, KEY_GPLACE_URL, KEY_LAT, KEY_LON, KEY_DISTANCE, KEY_IN_ITINERARY, 
			KEY_NOTES, KEY_RATING };

	private static final String TAG = "VineyardsDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_CREATE = "create table vineyards (_id integer primary key autoincrement, "
			+ "state text not null, name text not null, address text not null, phone text not null, "
			+ "url text not null, twitter text not null, facebook text not null, gplace_url text not null, "
			+ "lat text not null, lon text not null, distance text not null, in_itinerary integer not null, "
			+ "notes text not null, rating integer not null);";

	private static final String DATABASE_NAME = "db_data";
	private static final String DATABASE_TABLE = "vineyards";
	private static final int DATABASE_VERSION = 2;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS vineyards");
			onCreate(db);
		}
	}

	public VineyardsDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public VineyardsDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long createRecord(HashMap<String, String> record) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_STATE, record.get("state"));
		initialValues.put(KEY_NAME, record.get("name"));
		initialValues.put(KEY_ADDRESS, record.get("address"));
		initialValues.put(KEY_PHONE, record.get("phone"));
		initialValues.put(KEY_URL, record.get("url"));
		initialValues.put(KEY_TWITTER, record.get("twitter"));
		initialValues.put(KEY_FACEBOOK, record.get("facebook"));
		initialValues.put(KEY_GPLACE_URL, record.get("gplace_url"));
		initialValues.put(KEY_LAT, record.get("lat"));
		initialValues.put(KEY_LON, record.get("lon"));
		initialValues.put(KEY_DISTANCE, record.get("distance"));
		initialValues.put(KEY_IN_ITINERARY, 0);
		initialValues.put(KEY_NOTES, "");
		initialValues.put(KEY_RATING, 0);

		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	public boolean createRecords(ArrayList<HashMap<String, String>> records) {
		Boolean result = true;
		try {
			mDb.beginTransaction();
			for (int i = 0; i < records.size(); i++) {
				if (this.createRecord(records.get(i)) == -1)
					result = false;
			}
			mDb.setTransactionSuccessful();
		} catch (SQLException e) {
		} finally {
			mDb.endTransaction();
		}
		return result;
	}

	public boolean deleteRecords(String where, String[] whereArgs) {
		return mDb.delete(DATABASE_TABLE, where, whereArgs) > 0;
	}

	public Cursor fetchRecords(String where, String[] whereArgs) {
		return mDb.query(DATABASE_TABLE, allColumns, where, whereArgs, null,
				null, null);
	}
	
	public ArrayList<HashMap<String, String>> getRecords(String where, String[] whereArgs) {
		ArrayList<HashMap<String, String>> records = new ArrayList<HashMap<String, String>>();
		Cursor cursor = this.fetchRecords(where, whereArgs);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			HashMap<String, String> record = new HashMap<String, String>();
			for(int i=0; i<allColumns.length; i++){
				record.put(allColumns[i], cursor.getString(cursor.getColumnIndexOrThrow(allColumns[i])));
			}
			records.add(record);
			cursor.moveToNext();
		}
		cursor.close();
		return records;
	}

	public Cursor fetchRecord(long rowId) throws SQLException {
		Cursor cursor = mDb.query(true, DATABASE_TABLE, allColumns, KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public HashMap<String, String> getRecord(long rowId) {
		HashMap<String, String> record = new HashMap<String, String>();
		Cursor cursor = this.fetchRecord(rowId);
		for(int i=0; i<allColumns.length; i++){
			record.put(allColumns[i], cursor.getString(cursor.getColumnIndexOrThrow(allColumns[i])));
		}
		cursor.close();
		return record;
	}

	public boolean updateRecord(HashMap<String, String> record) {
		ContentValues values = new ContentValues();
		values.put(KEY_STATE, record.get("state"));
		values.put(KEY_NAME, record.get("name"));
		values.put(KEY_ADDRESS, record.get("address"));
		values.put(KEY_PHONE, record.get("phone"));
		values.put(KEY_URL, record.get("url"));
		values.put(KEY_TWITTER, record.get("twitter"));
		values.put(KEY_FACEBOOK, record.get("facebook"));
		values.put(KEY_GPLACE_URL, record.get("gplace_url"));
		values.put(KEY_LAT, record.get("lat"));
		values.put(KEY_LON, record.get("lon"));
		values.put(KEY_DISTANCE, record.get("distance"));
		values.put(KEY_IN_ITINERARY, record.get("in_itinerary"));
		values.put(KEY_NOTES, record.get("notes"));
		values.put(KEY_RATING, record.get("rating"));

		return mDb.update(DATABASE_TABLE, values, KEY_ROWID + "=" + record.get("_id"), null) > 0;
	}
	
	public boolean updateRecords(ArrayList<HashMap<String, String>> records) {
		Boolean result = true;
		try {
			mDb.beginTransaction();
			for (int i = 0; i < records.size(); i++) {
				if (!this.updateRecord(records.get(i)))
					result = false;
			}
			mDb.setTransactionSuccessful();
		} catch (SQLException e) {
		} finally {
			mDb.endTransaction();
		}
		return result;
	}

}