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

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.techjoynt.android.nxt.R;
import com.techjoynt.android.nxt.TechjoyntApplication;
import com.techjoynt.android.nxt.fragment.NXTFragment;
import com.techjoynt.android.nxt.fragment.SpheroFragment;
import com.techjoynt.android.nxt.fragment.dialog.DeviceSwitchFragment;
import com.techjoynt.android.nxt.fragment.dialog.DeviceSwitchFragment.SelectedDeviceListener;
import com.techjoynt.android.nxt.prefs.Preferences;
import com.techjoynt.android.nxt.util.Util;

public class RemoteControl extends SherlockFragmentActivity implements Runnable, SelectedDeviceListener {
	private static final String NXT = "NXT";
	private static final String SPHERO = "Sphero";
	
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	private static SharedPreferences mPrefs = TechjoyntApplication.getPrefs();
	// TODO: private static SharedPreferences.Editor editor = mPrefs.edit();
	
	private static int TIMEOUT_POLL_PERIOD = 15000; // 15 seconds
	private static int TIMEOUT_PERIOD = 300000; // 5 minutes
	private View content = null;
	private long lastActivity = SystemClock.uptimeMillis();
	
	private static final int REQUEST_ENABLE_BT = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		content = findViewById(android.R.id.content);
		content.setKeepScreenOn(true);
		run();
		
		
		if (mBluetoothAdapter != null) {
			if (mBluetoothAdapter.isEnabled()) {
				checkForDefault();
			}
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (mBluetoothAdapter == null) {
			noBluetoothOnDevice().show();
		} else if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		content.removeCallbacks(this);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
        switch (requestCode) {
        
        case REQUEST_ENABLE_BT:
        	if (resultCode == Activity.RESULT_OK) {
        		checkForDefault();
        		
        	} else if (resultCode == Activity.RESULT_CANCELED) {
        		bluetoothNotTurnedOn().show();
        	}
        }
	}
	
	private void selectDevice() {
		SherlockDialogFragment fragment = new DeviceSwitchFragment();
		fragment.show(getSupportFragmentManager(), "device_switch");
	}
	
	private void checkForDefault() {
		if (!Util.isDefaultSelected()) {
			selectDevice();
		} else {
			if (mPrefs.getString(Preferences.KEY_PREF_DEFAULT_DEVICE_TYPE, "").equals("NXT")) {
				SherlockFragment nxtFragment = new NXTFragment();
				replaceFragment(nxtFragment);
			} else {
				SherlockFragment spheroFragment = new SpheroFragment();
				replaceFragment(spheroFragment);
				
				/**
				notActiveFeature().show();
				editor.putInt(Preferences.KEY_PREF_SELECTED_DEVICE, 0).commit(); **/
			}
		}
	}
	
	@Override
	public void onDeviceSelectedChanged(String deviceType) {
		if (deviceType.equals(NXT)) {
			SherlockFragment nxtFragment = new NXTFragment();
			replaceFragment(nxtFragment);
		} else if (deviceType.equals(SPHERO)) {
			SherlockFragment spheroFragment = new SpheroFragment();
			replaceFragment(spheroFragment);
			
			/**
			notActiveFeature().show();
			editor.putInt(Preferences.KEY_PREF_SELECTED_DEVICE, 0).commit(); **/
		}
	}
	
	private void replaceFragment(Fragment fragment) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, fragment);
		transaction.commit();
	}
	
	@Override
	public void run() {
		if ((SystemClock.uptimeMillis() - lastActivity) > TIMEOUT_PERIOD) {
			content.setKeepScreenOn(false);
		}
		content.postDelayed(this, TIMEOUT_POLL_PERIOD);
	}

	public void onClick(View v) {
		lastActivity = SystemClock.uptimeMillis();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.remote_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.device_switch:
			selectDevice();
			return true;
		case R.id.preferences:
			Intent intent = new Intent(this, Preferences.class);
			startActivity(intent);
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private AlertDialog noBluetoothOnDevice() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(R.string.no_bluetooth);
		builder.setIcon(R.drawable.ic_action_bluetooth);
		builder.setMessage(R.string.no_bluetooth_message);
		
		builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
			}
		});
		
		return builder.create();
	}
	
	private AlertDialog bluetoothNotTurnedOn() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(R.string.bluetooth_required);
		builder.setIcon(R.drawable.ic_action_bluetooth);
		builder.setMessage(R.string.bluetooth_required_message);
		
		builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
			}
		});
		
		return builder.create();
	}
	/** TODO:
	private AlertDialog notActiveFeature() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(R.string.future_feature);
		builder.setIcon(R.drawable.ic_action_emo_wink);
		builder.setMessage(R.string.future_feature_message);
		
		builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		return builder.create();
	} **/
}