package com.techjoynt.android.nxt.util;

import android.content.SharedPreferences;

import com.techjoynt.android.nxt.TechjoyntApplication;
import com.techjoynt.android.nxt.prefs.Preferences;

public class Util {
	private static SharedPreferences mPrefs = TechjoyntApplication.getPrefs();
	//private static SharedPreferences.Editor editor = mPrefs.edit();
	
	public static boolean isDefaultSelected() {
		if (mPrefs.getString(Preferences.KEY_PREF_DEFAULT_DEVICE_TYPE, "").isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
}
