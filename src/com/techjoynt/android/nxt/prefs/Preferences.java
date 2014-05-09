package com.techjoynt.android.nxt.prefs;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class Preferences extends ActionBarActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		openPrefs();
	}
	
	private void openPrefs() {
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFragment()).commit();
    }
}