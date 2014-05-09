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

package com.techjoynt.android.nxt.fragment;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.techjoynt.android.nxt.R;
import com.techjoynt.android.nxt.fragment.dialog.NXTSelectionFragment;
import com.techjoynt.android.nxt.http.NXTTalker;

public class NXTFragment extends Fragment implements OnSharedPreferenceChangeListener {
    private boolean NO_BT = false;
	
	private static final int REQUEST_ENABLE_BT = 1;

	public static final int MESSAGE_TOAST = 1;
	public static final int MESSAGE_STATE_CHANGE = 2;

	public static final String TOAST = "toast";

	private static final int MODE_BUTTONS = 1;
	
	private NXTTalker mNXTTalker;

	private int mState = NXTTalker.STATE_NONE;
	private int mSavedState = NXTTalker.STATE_NONE;
	private boolean mNewLaunch = true;
	private String mDeviceAddress = null;
	private TextView mStateDisplay;
	private Button mConnectButton;
	private Button mDisconnectButton;

	private int mPower = 80;
	private int mControlsMode = MODE_BUTTONS;

	private boolean mReverse;
	private boolean mReverseLR;
	private boolean mRegulateSpeed;
	private boolean mSynchronizeMotors;
	
    private BluetoothAdapter mBluetoothAdapter;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
		return inflater.inflate(R.layout.fragment_nxt_rc, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		readPreferences(prefs, null);
		prefs.registerOnSharedPreferenceChangeListener(this);

		if (savedInstanceState != null) {
			mNewLaunch = false;
			mDeviceAddress = savedInstanceState.getString("device_address");
			if (mDeviceAddress != null) {
				mSavedState = NXTTalker.STATE_CONNECTED;
			}

			if (savedInstanceState.containsKey("power")) {
				mPower = savedInstanceState.getInt("power");
			}
			if (savedInstanceState.containsKey("controls_mode")) {
				mControlsMode = savedInstanceState.getInt("controls_mode");
			}
		}
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	       
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
            return;
        }
        
		setupUI();
		mNXTTalker = new NXTTalker(mHandler);
	}
	
	@Override
	public void onStart() {
        super.onStart();
        
        Bundle args = getArguments();
        
        if (args != null) {
        	connectToNXT(args.getString("device_address"));
        } else {
        	if (!mBluetoothAdapter.isEnabled()) {
        		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                if (mSavedState == NXTTalker.STATE_CONNECTED) {
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                    mNXTTalker.connect(device);
                } else {
                    if (mNewLaunch) {
                        mNewLaunch = false;
                        findBrick();
                    }
                }
            }
        }
	}
	
	@Override
	public void onStop() {
        super.onStop();
        mSavedState = mState;
        mNXTTalker.stop();
    }
	
	@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        readPreferences(sharedPreferences, key);
    }
	
	public void connectToNXT(String address) {
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		mDeviceAddress = address;
		mNXTTalker.connect(device);
	}
    
    private void readPreferences(SharedPreferences prefs, String key) {
        if (key == null) {
            mReverse = prefs.getBoolean("PREF_SWAP_FWDREV", false);
            mReverseLR = prefs.getBoolean("PREF_SWAP_LEFTRIGHT", false);
            mRegulateSpeed = prefs.getBoolean("PREF_REG_SPEED", false);
            mSynchronizeMotors = prefs.getBoolean("PREF_REG_SYNC", false);
            if (!mRegulateSpeed) {
                mSynchronizeMotors = false;
            }
        } else if (key.equals("PREF_SWAP_FWDREV")) {
            mReverse = prefs.getBoolean("PREF_SWAP_FWDREV", false);
        } else if (key.equals("PREF_SWAP_LEFTRIGHT")) {
            mReverseLR = prefs.getBoolean("PREF_SWAP_LEFTRIGHT", false);
        } else if (key.equals("PREF_REG_SPEED")) {
            mRegulateSpeed = prefs.getBoolean("PREF_REG_SPEED", false);
            if (!mRegulateSpeed) {
                mSynchronizeMotors = false;
            }
        } else if (key.equals("PREF_REG_SYNC")) {
            mSynchronizeMotors = prefs.getBoolean("PREF_REG_SYNC", false);
        }
    }

	private void findBrick() {
		DialogFragment fragment = new NXTSelectionFragment();
		fragment.show(getFragmentManager(), "findBrick");
	}
	
	private void setupUI() {
		ImageButton buttonUp = (ImageButton) getView().findViewById(R.id.button_up);
        buttonUp.setOnTouchListener(new DirectionButtonOnTouchListener(1, 1));
        ImageButton buttonLeft = (ImageButton) getView().findViewById(R.id.button_left);
        buttonLeft.setOnTouchListener(new DirectionButtonOnTouchListener(-0.6, 0.6));
        ImageButton buttonDown = (ImageButton) getView().findViewById(R.id.button_down);
        buttonDown.setOnTouchListener(new DirectionButtonOnTouchListener(-1, -1));
        ImageButton buttonRight = (ImageButton) getView().findViewById(R.id.button_right);
        buttonRight.setOnTouchListener(new DirectionButtonOnTouchListener(0.6, -0.6));

        SeekBar powerSeekBar = (SeekBar) getView().findViewById(R.id.power_seekbar);
        powerSeekBar.setProgress(mPower);
        powerSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPower = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }            
        });
        
        mStateDisplay = (TextView) getView().findViewById(R.id.state_display);

        mConnectButton = (Button) getView().findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NO_BT) {
                    findBrick();
                } else {
                    mState = NXTTalker.STATE_CONNECTED;
                    displayState();
                }
            }
        });
        
        mDisconnectButton = (Button) getView().findViewById(R.id.disconnect_button);
        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNXTTalker.stop();
            }
        });

        displayState();
	}
	
	@SuppressLint("HandlerLeak") 
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_TOAST:
                Toast.makeText(getActivity().getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_STATE_CHANGE:
                mState = msg.arg1;
                displayState();
                break;
            }
        }
    };
    
    @Override
	public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mState == NXTTalker.STATE_CONNECTED) {
            outState.putString("device_address", mDeviceAddress);
        }
        outState.putInt("power", mPower);
        outState.putInt("controls_mode", mControlsMode);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setupUI();
    }
    
    private void displayState() {
        String stateText = null;
        int color = 0;
        switch (mState) { 
        
        case NXTTalker.STATE_NONE:
            stateText = "Not connected";
            color = 0xffff0000;
            mConnectButton.setVisibility(View.VISIBLE);
            mDisconnectButton.setVisibility(View.GONE);
            if (!isDetached() && isVisible()) {
            	getActivity().setProgressBarIndeterminateVisibility(false); 
            }
            break;
        case NXTTalker.STATE_CONNECTING:
            stateText = "Connecting...";
            color = 0xffffff00;
            mConnectButton.setVisibility(View.GONE);
            mDisconnectButton.setVisibility(View.GONE);
            
            if (!isDetached() && isVisible()) {
            	getActivity().setProgressBarIndeterminateVisibility(true); 	
            }
            break;
        case NXTTalker.STATE_CONNECTED:
            stateText = "Connected";
            color = 0xff00ff00;
            mConnectButton.setVisibility(View.GONE);
            mDisconnectButton.setVisibility(View.VISIBLE);
            if (!isDetached() && isVisible()) {
            	getActivity().setProgressBarIndeterminateVisibility(false); 
            }
            break;
        }
        mStateDisplay.setText(stateText);
        mStateDisplay.setTextColor(color);
    }

	private class DirectionButtonOnTouchListener implements OnTouchListener {

		private double lmod;
		private double rmod;

		public DirectionButtonOnTouchListener(double l, double r) {
			lmod = l;
			rmod = r;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// Log.i("NXT", "onTouch event: " +
			// Integer.toString(event.getAction()));
			int action = event.getAction();
			// if ((action == MotionEvent.ACTION_DOWN) || (action ==
			// MotionEvent.ACTION_MOVE)) {
			if (action == MotionEvent.ACTION_DOWN) {
				byte power = (byte) mPower;
				if (mReverse) {
					power *= -1;
				}
				byte l = (byte) (power * lmod);
				byte r = (byte) (power * rmod);
				if (!mReverseLR) {
					mNXTTalker.motors(l, r, mRegulateSpeed, mSynchronizeMotors);
				} else {
					mNXTTalker.motors(r, l, mRegulateSpeed, mSynchronizeMotors);
				}
			} else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
				mNXTTalker.motors((byte) 0, (byte) 0, mRegulateSpeed, mSynchronizeMotors);
			}
			return true;
		}
	}
}