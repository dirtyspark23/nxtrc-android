/*
 * Copyright (c) 2014 - DeAngelo Mannie | Intravita LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.techjoynt.android.nxt.activity;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bugsnag.android.Bugsnag;
import com.techjoynt.android.nxt.R;
import com.techjoynt.android.nxt.prefs.Preferences;

public class AboutActivity extends SherlockActivity {
	private TextView mVersion;
	private PackageInfo info;
	
	private String versionInfo;
	private ImageView techjoynt, intravita;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		configureActionBar();
		
		try {
			setupTextViews();
		} catch (NameNotFoundException e) {
			Bugsnag.notify(e);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Intent upIntent = new Intent(this, Preferences.class);
				NavUtils.navigateUpTo(this, upIntent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void configureActionBar() {
		ActionBar actionBar = getSupportActionBar();	
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.about_label);
	}
	
	private void setupTextViews() throws NameNotFoundException {
		info = getPackageManager().getPackageInfo(getPackageName(), 0);
		versionInfo = info.versionName;
		
		techjoynt = (ImageView) findViewById(R.id.techjoynt_logo);
		intravita = (ImageView) findViewById(R.id.intravita_logo);
		
		mVersion = (TextView)findViewById(R.id.version);
		mVersion.setText("Version " + versionInfo);
		
		techjoynt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://techjoynt.com"));
				startActivity(i);
			}
		});
		
		intravita.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://intravita.com"));
				startActivity(i);
			}
		});
	}
}