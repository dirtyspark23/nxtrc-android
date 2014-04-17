package com.techjoynt.android.nxt.prefs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.techjoynt.android.nxt.R;
import com.techjoynt.android.nxt.activity.RemoteControl;
import com.techjoynt.android.nxt.util.ActivityHelper;

public class Preferences extends PreferenceActivity {
	public static final String KEY_PREF_SELECTED_DEVICE = "KEY_PREF_SELECTED_DEVICE";
	public static final String KEY_PREF_DEFAULT_DEVICE_TYPE = "KEY_PREF_DEFAULT_DEVICE_TYPE";
	public static final String KEY_PREF_ABOUT = "KEY_PREF_ABOUT";
	
	private CheckBoxPreference cb_sync;
	private Preference about;
	
	private Context mContext;

    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = this;
        CheckBoxPreference cb_speed = (CheckBoxPreference) findPreference("PREF_REG_SPEED");
        cb_sync = (CheckBoxPreference) findPreference("PREF_REG_SYNC");
        about = (Preference) findPreference(KEY_PREF_ABOUT);
        
        cb_speed.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!((Boolean) newValue).booleanValue()) {
                    cb_sync.setChecked(false);
                }
                return true;
            }
        });
        
        about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ActivityHelper.toAbout(mContext);
				return true;
			}
        });
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	Intent upIntent = new Intent(this, RemoteControl.class);
	        	NavUtils.navigateUpTo(this, upIntent);
	            return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}