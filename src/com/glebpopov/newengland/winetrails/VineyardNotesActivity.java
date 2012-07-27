package com.glebpopov.newengland.winetrails;

import java.util.HashMap;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.glebpopov.newengland.winetrails.db.VineyardsDbAdapter;

public class VineyardNotesActivity extends MainActivity {
	private VineyardsDbAdapter mDbHelper;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vineyard_notes);

		mDbHelper = new VineyardsDbAdapter(this);
		mDbHelper.open();

		TextView menuText = (TextView) findViewById(R.id.menu_text);
		menuText.setText("Notes");

		initTopMenu(new Integer[] { R.id.home_btn, R.id.back_btn });
		
		String id = getIntent().getExtras().getString("id");
		final HashMap<String, String> vineyard = mDbHelper.getRecord(Long.parseLong(id));

		TextView vname = (TextView) findViewById(R.id.vn_name);
		vname.setText(vineyard.get("name"));

		final EditText notes = (EditText) findViewById(R.id.vn_notes);
		notes.setText(vineyard.get("notes"));

		Button saveButton = (Button) findViewById(R.id.vn_save);
		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				vineyard.put("notes", notes.getText().toString());
				if (mDbHelper.updateRecord(vineyard)){
					Toast.makeText(VineyardNotesActivity.this, "Saved successfully!", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}
}
