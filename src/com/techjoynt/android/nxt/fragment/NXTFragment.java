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
import android.app.Activity;
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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.techjoynt.android.nxt.R;
import com.techjoynt.android.nxt.activity.dialog.ChooseNXTDevice;
import com.techjoynt.android.nxt.http.NXTTalker;
import com.techjoynt.android.nxt.prefs.Preferences;

public class NXTFragment extends SherlockFragment implements OnSharedPreferenceChangeListener {
    private boolean NO_BT = false;
	
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_CONNECT_DEVICE = 2;

	public static final int MESSAGE_TOAST = 1;
	public static final int MESSAGE_STATE_CHANGE = 2;

	public static final String TOAST = "toast";

	private static final int MODE_BUTTONS = 1;
	private static final int MODE_TOUCHPAD = 2;
	private static final int MODE_TANK = 3;
	private static final int MODE_TANK3MOTOR = 4;
	
	private NXTTalker mNXTTalker;

	private int mState = NXTTalker.STATE_NONE;
	private int mSavedState = NXTTalker.STATE_NONE;
	private boolean mNewLaunch = true;
	private String mDeviceAddress = null;
	private TextView mStateDisplay;
	private Button mConnectButton;
	private Button mDisconnectButton;
	//private TouchPadView mTouchPadView;
	//private TankView mTankView;
	//private Tank3MotorView mTank3MotorView;
	//private Menu mMenu;

	private int mPower = 80;
	private int mControlsMode = MODE_BUTTONS;

	private boolean mReverse;
	private boolean mReverseLR;
	private boolean mRegulateSpeed;
	private boolean mSynchronizeMotors;
	
    private BluetoothAdapter mBluetoothAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

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
	
	@Override
	public void onStop() {
        super.onStop();
        mSavedState = mState;
        mNXTTalker.stop();
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.nxt_menu, menu);
	}
	
	 @Override 
	 public boolean onOptionsItemSelected(MenuItem item) {   
		 switch (item.getItemId()) {
	        	
		 case R.id.menuitem_buttons:
			 mControlsMode = MODE_BUTTONS;
			 setupUI();
			 return true;
		 case R.id.menuitem_touchpad:
			 mControlsMode = MODE_TOUCHPAD;
			 setupUI();
			 return false;
		 case R.id.menuitem_tank:
			 mControlsMode = MODE_TANK;
			 setupUI();
			 return false;
		 case R.id.menuitem_tank3motor:
			 mControlsMode = MODE_TANK3MOTOR;
			 setupUI();
			 return false;
		 case R.id.menuitem_settings:
			 Intent i = new Intent(getActivity(), Preferences.class);
			 startActivity(i);
			 return true;
		 default:
			 return false;    
		 }
	 }
	
	@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        readPreferences(sharedPreferences, key);
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
		Intent intent = new Intent(getActivity(), ChooseNXTDevice.class);
        startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
        switch (requestCode) {
        
        case REQUEST_ENABLE_BT:
            if (resultCode == Activity.RESULT_OK) {
                findBrick();
            } else {
                Toast.makeText(getActivity(), "Bluetooth not enabled, exiting.", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
            break;
        
        case REQUEST_CONNECT_DEVICE:
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras().getString(ChooseNXTDevice.EXTRA_DEVICE_ADDRESS);
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                mDeviceAddress = address;
                mNXTTalker.connect(device);
            }
            break;
        }
    }
    
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
            	getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false); 
            }
            break;
        case NXTTalker.STATE_CONNECTING:
            stateText = "Connecting...";
            color = 0xffffff00;
            mConnectButton.setVisibility(View.GONE);
            mDisconnectButton.setVisibility(View.GONE);
            
            if (!isDetached() && isVisible()) {
            	getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true); 	
            }
            break;
        case NXTTalker.STATE_CONNECTED:
            stateText = "Connected";
            color = 0xff00ff00;
            mConnectButton.setVisibility(View.GONE);
            mDisconnectButton.setVisibility(View.VISIBLE);
            if (!isDetached() && isVisible()) {
            	getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false); 
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
	
	/**
	private class TankOnTouchListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			TankView tv = (TankView) v;
			float y;
			int action = event.getAction();
			if ((action == MotionEvent.ACTION_DOWN)
					|| (action == MotionEvent.ACTION_MOVE)) {
				byte l = 0;
				byte r = 0;
				for (int i = 0; i < event.getPointerCount(); i++) {
					y = -1.0f * (event.getY(i) - tv.mZero) / tv.mRange;
					if (y > 1.0f) {
						y = 1.0f;
					}
					if (y < -1.0f) {
						y = -1.0f;
					}
					if (event.getX(i) < tv.mWidth / 2f) {
						l = (byte) (y * 100);
					} else {
						r = (byte) (y * 100);
					}
				}
				if (mReverse) {
					l *= -1;
					r *= -1;
				}
				if (!mReverseLR) {
					mNXTTalker.motors(l, r, mRegulateSpeed, mSynchronizeMotors);
				} else {
					mNXTTalker.motors(r, l, mRegulateSpeed, mSynchronizeMotors);
				}
			} else if ((action == MotionEvent.ACTION_UP)
					|| (action == MotionEvent.ACTION_CANCEL)) {
				mNXTTalker.motors((byte) 0, (byte) 0, mRegulateSpeed,
						mSynchronizeMotors);
			}
			return true;
		}
	}

	private class Tank3MotorOnTouchListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Tank3MotorView t3v = (Tank3MotorView) v;
			float x;
			float y;
			int action = event.getAction();
			if ((action == MotionEvent.ACTION_DOWN)
					|| (action == MotionEvent.ACTION_MOVE)) {
				byte l = 0;
				byte r = 0;
				byte a = 0;
				for (int i = 0; i < event.getPointerCount(); i++) {
					y = -1.0f * (event.getY(i) - t3v.mZero) / t3v.mRange;
					if (y > 1.0f) {
						y = 1.0f;
					}
					if (y < -1.0f) {
						y = -1.0f;
					}
					x = event.getX(i);
					if (x < t3v.mWidth / 3f) {
						l = (byte) (y * 100);
					} else if (x > 2 * t3v.mWidth / 3f) {
						r = (byte) (y * 100);
					} else {
						a = (byte) (y * 100);
					}
				}
				if (mReverse) {
					l *= -1;
					r *= -1;
					a *= -1;
				}
				if (!mReverseLR) {
					mNXTTalker.motors3(l, r, a, mRegulateSpeed,
							mSynchronizeMotors);
				} else {
					mNXTTalker.motors3(r, l, a, mRegulateSpeed,
							mSynchronizeMotors);
				}
			} else if ((action == MotionEvent.ACTION_UP)
					|| (action == MotionEvent.ACTION_CANCEL)) {
				mNXTTalker.motors3((byte) 0, (byte) 0, (byte) 0,
						mRegulateSpeed, mSynchronizeMotors);
			}
			return true;
		}
	}

	private class TouchpadOnTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			TouchPadView tpv = (TouchPadView) v;
			float x, y, power;
			int action = event.getAction();
			if ((action == MotionEvent.ACTION_DOWN)
					|| (action == MotionEvent.ACTION_MOVE)) {
				x = (event.getX() - tpv.mCx) / tpv.mRadius;
				y = -1.0f * (event.getY() - tpv.mCy);
				if (y > 0f) {
					y -= tpv.mOffset;
					if (y < 0f) {
						y = 0.01f;
					}
				} else if (y < 0f) {
					y += tpv.mOffset;
					if (y > 0f) {
						y = -0.01f;
					}
				}
				y /= tpv.mRadius;
				float sqrt22 = 0.707106781f;
				float nx = x * sqrt22 + y * sqrt22;
				float ny = -x * sqrt22 + y * sqrt22;
				power = (float) Math.sqrt(nx * nx + ny * ny);
				if (power > 1.0f) {
					nx /= power;
					ny /= power;
					power = 1.0f;
				}
				float angle = (float) Math.atan2(y, x);
				float l, r;
				if (angle > 0f && angle <= Math.PI / 2f) {
					l = 1.0f;
					r = (float) (2.0f * angle / Math.PI);
				} else if (angle > Math.PI / 2f && angle <= Math.PI) {
					l = (float) (2.0f * (Math.PI - angle) / Math.PI);
					r = 1.0f;
				} else if (angle < 0f && angle >= -Math.PI / 2f) {
					l = -1.0f;
					r = (float) (2.0f * angle / Math.PI);
				} else if (angle < -Math.PI / 2f && angle > -Math.PI) {
					l = (float) (-2.0f * (angle + Math.PI) / Math.PI);
					r = -1.0f;
				} else {
					l = r = 0f;
				}
				l *= power;
				r *= power;
				if (mReverse) {
					l *= -1;
					r *= -1;
				}
				if (!mReverseLR) {
					mNXTTalker.motors((byte) (100 * l), (byte) (100 * r),
							mRegulateSpeed, mSynchronizeMotors);
				} else {
					mNXTTalker.motors((byte) (100 * r), (byte) (100 * l),
							mRegulateSpeed, mSynchronizeMotors);
				}
			} else if ((action == MotionEvent.ACTION_UP)
					|| (action == MotionEvent.ACTION_CANCEL)) {
				mNXTTalker.motors((byte) 0, (byte) 0, mRegulateSpeed,
						mSynchronizeMotors);
			}
			return true;
		}
	} **/
}