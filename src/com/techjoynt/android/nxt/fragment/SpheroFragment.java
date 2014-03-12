package com.techjoynt.android.nxt.fragment;

import java.util.List;

import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.sphero.CollisionListener;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.DiscoveryListener;
import orbotix.sphero.PersistentOptionFlags;
import orbotix.sphero.SensorControl;
import orbotix.sphero.SensorFlag;
import orbotix.sphero.SensorListener;
import orbotix.sphero.Sphero;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.techjoynt.android.nxt.R;

public class SpheroFragment extends SherlockFragment implements View.OnClickListener {
	private static final String TAG = SpheroFragment.class.getSimpleName();
	private Sphero mRobot;
	private Button top,bottom,left,right,stop;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
		View v = inflater.inflate(R.layout.fragment_sphero_rc, container, false);
		
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle icicle) {
		super.onActivityCreated(icicle);
		
		top = (Button) getView().findViewById(R.id.zero_button);
		top.setOnClickListener(this);
		bottom = (Button) getView().findViewById(R.id.one_eighty_button);
		bottom.setOnClickListener(this);
		left = (Button) getView().findViewById(R.id.two_seventy_button);
		left.setOnClickListener(this);
		right = (Button) getView().findViewById(R.id.ninety_button);
		right.setOnClickListener(this);
		stop = (Button) getView().findViewById(R.id.stop_button);
		stop.setOnClickListener(this);
	}

    /** Called when the user comes back to this app */
    @Override
	public void onResume() {
        super.onResume();
        RobotProvider.getDefaultProvider().addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(Robot robot) {
                // Save the robot
                mRobot = (Sphero) robot;
                // Start the connected method
                connected();
            }

            @Override
            public void onConnectionFailed(Robot sphero) {
                Log.d(TAG, "Connection Failed: " + sphero);
                Toast.makeText(getActivity(), "Sphero Connection Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDisconnected(Robot robot) {
                Log.d(TAG, "Disconnected: " + robot);
                Toast.makeText(getActivity(), "Sphero Disconnected", Toast.LENGTH_SHORT).show();
                //stopBlink();
                mRobot = null;
            }
        });
        
        RobotProvider.getDefaultProvider().addDiscoveryListener(new DiscoveryListener() {
            @Override
            public void onBluetoothDisabled() {
                Log.d(TAG, "Bluetooth Disabled");
                Toast.makeText(getActivity(), "Bluetooth Disabled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void discoveryComplete(List<Sphero> spheros) {
                Log.d(TAG, "Found " + spheros.size() + " robots");
            }

            @Override
            public void onFound(List<Sphero> sphero) {
                Log.d(TAG, "Found: " + sphero);
                RobotProvider.getDefaultProvider().connect(sphero.iterator().next());
            }
        });

       
        boolean success = RobotProvider.getDefaultProvider().startDiscovery(getActivity());
        
        if(!success) {
            Toast.makeText(getActivity(), "Unable To start Discovery!", Toast.LENGTH_LONG).show();
        }
    }
    
    /** Called when the user presses the back or home button */
    @Override
	public void onPause() {
        super.onPause();
        this.stopBlink();
        if (mRobot != null) {
            mRobot.disconnect();
        }
    }

    boolean blinking = true;

    private void stopBlink() {
        blinking = false;
    }

    /**
     * Causes the robot to blink once every second.
     *
     * @param lit
     */
    private void blink(final boolean lit) {
        if (mRobot == null) {
            blinking = false;
            return;
        }

        //If not lit, send command to show blue light, or else, send command to show no light
        if (lit) {
            mRobot.setColor(0, 0, 0);

        } else {
            mRobot.setColor(0, 255, 0);
        }

        if (blinking) {
            //Send delayed message on a handler to run blink again
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    blink(!lit);
                }
            }, 2000);
        }
    }

    /**
     * When the user clicks "STOP", stop the Robot.
     *
     * @param v The View that had been clicked
     */
    public void onStopClick(View v) {
        if (mRobot != null) {
            // Stop robot
            mRobot.stop();
        }
    }

    /**
     * When the user clicks a control button, roll the Robot in that direction
     *
     * @param v The View that had been clicked
     */
    public void onClick(View v) {
        // Find the heading, based on which button was clicked
        final float heading;
        switch (v.getId()) {

            case R.id.ninety_button:
                heading = 90f;
                break;

            case R.id.one_eighty_button:
                heading = 180f;
                break;

            case R.id.two_seventy_button:
                heading = 270f;
                break;

            default:
                heading = 0f;
                break;
        }

        // Set speed. 60% of full speed
        final float speed = 0.6f;

        // Roll robot
        mRobot.drive(heading, speed);
    }
    
    private void connected() {
        Log.d(TAG, "Connected On Thread: " + Thread.currentThread().getName());
        Log.d(TAG, "Connected: " + mRobot);
        Toast.makeText(getActivity(), mRobot.getName() + " Connected", Toast.LENGTH_LONG).show();

        final SensorControl control = mRobot.getSensorControl();
        control.addSensorListener(new SensorListener() {
            @Override
            public void sensorUpdated(DeviceSensorsData sensorDataArray) {
                Log.d(TAG, sensorDataArray.toString());
            }
        } ,SensorFlag.ACCELEROMETER_NORMALIZED, SensorFlag.GYRO_NORMALIZED);

        control.setRate(1);
        mRobot.enableStabilization(false);
        mRobot.drive(90, 0);
        mRobot.setBackLEDBrightness(.5f);

        mRobot.getCollisionControl().startDetection(255,255,255,255,255);
        mRobot.getCollisionControl().addCollisionListener(new CollisionListener() {
            public void collisionDetected(CollisionDetectedAsyncData collisionData) {
                Log.d(TAG, collisionData.toString());
            }
        });

        blink(false); // Blink the robot's LED

        boolean preventSleepInCharger = mRobot.getConfiguration().isPersistentFlagEnabled(PersistentOptionFlags.PreventSleepInCharger);
        Log.d(TAG, "Prevent Sleep in charger = " + preventSleepInCharger);
        Log.d(TAG, "VectorDrive = " + mRobot.getConfiguration().isPersistentFlagEnabled(PersistentOptionFlags.EnableVectorDrive));

        mRobot.getConfiguration().setPersistentFlag(PersistentOptionFlags.PreventSleepInCharger, false);
        mRobot.getConfiguration().setPersistentFlag(PersistentOptionFlags.EnableVectorDrive, true);

        Log.d(TAG, "VectorDrive = " + mRobot.getConfiguration().isPersistentFlagEnabled(PersistentOptionFlags.EnableVectorDrive));
        Log.v(TAG, mRobot.getConfiguration().toString());

    }
}