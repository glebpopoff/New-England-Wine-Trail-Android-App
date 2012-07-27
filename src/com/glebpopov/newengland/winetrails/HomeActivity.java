package com.glebpopov.newengland.winetrails;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.glebpopov.newengland.winetrails.adapters.StateItemAdapter;

public class HomeActivity extends MainActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initBottomMenu(false);

		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(new StateItemAdapter(this));

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(isNetworkAvailable()){
					Intent showListIntent = new Intent(view.getContext(), VineyardsListActivity.class);
					showListIntent.putExtra("position", position);
					startActivityForResult(showListIntent, 0);
				}
				else Toast.makeText(getApplicationContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
			}
		});
	}

	/*
	 * @Override public boolean onPrepareOptionsMenu(Menu menu) { MenuItem
	 * quitItem = menu.findItem(R.id.back);
	 * quitItem.setIcon(R.drawable.ic_menu_close_clear_cancel);
	 * quitItem.setTitle("Quit"); return super.onPrepareOptionsMenu(menu); }
	 */
}