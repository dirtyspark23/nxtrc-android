/*
 * Copyright (c) 2013 - DeAngelo Mannie | Intravita LLC
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

package com.techjoynt.android.nxt.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.techjoynt.android.nxt.R;
import com.techjoynt.android.nxt.TechjoyntApplication;
import com.techjoynt.android.nxt.prefs.Preferences;

public class DeviceSwitchFragment extends DialogFragment {
	private SelectedDeviceListener mListener;
	
	private static SharedPreferences mPrefs = TechjoyntApplication.getPrefs();
	private static SharedPreferences.Editor editor = mPrefs.edit();
	
	String selectedDeviceType = "NXT";
	
	public interface SelectedDeviceListener {
		public void onDeviceSelectedChanged(String deviceType);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SelectedDeviceListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SelectedDeviceListener");
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle(R.string.choose_device_type);
		builder.setIcon(R.drawable.ic_action_info);
		builder.setSingleChoiceItems(R.array.robots_array, mPrefs.getInt(Preferences.KEY_PREF_SELECTED_DEVICE, -1), new DialogInterface.OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String robotNames[] = getResources().getStringArray(R.array.robots_array); 
				selectedDeviceType = robotNames[which].toString();
				editor.putInt(Preferences.KEY_PREF_SELECTED_DEVICE, which).commit();
				mListener.onDeviceSelectedChanged(selectedDeviceType);
				getDialog().dismiss();
			}
		});
		
		return builder.create();
	}
}