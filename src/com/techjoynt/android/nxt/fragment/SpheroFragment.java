package com.techjoynt.android.nxt.fragment;

import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.techjoynt.android.nxt.R;

public class SpheroFragment extends SherlockFragment {
	private Robot mRobot;
	
	private SpheroConnectionView mSpheroConnectionView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
		return inflater.inflate(R.layout.fragment_sphero_rc, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle icicle) {
		super.onActivityCreated(icicle);
		
		mSpheroConnectionView = (SpheroConnectionView)getView().findViewById(R.id.sphero_connection_view);
		
		// Set the connection event listener 
		mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
		    // If the user clicked a Sphero and it failed to connect, this event will be fired
		    @Override
		    public void onRobotConnectionFailed(Robot robot) {}
		    // If there are no Spheros paired to this device, this event will be fired
		    @Override
		    public void onNonePaired() {}
		    // The user clicked a Sphero and it successfully paired.
		    @Override
		    public void onRobotConnected(Robot robot) {
		        mRobot = robot;
		        // Skip this next step if you want the user to be able to connect multiple Spheros
		        mSpheroConnectionView.setVisibility(View.GONE);
		    }
		    
			@Override
			public void onBluetoothNotEnabled() {
				
			}
		});
	}
	
	@Override
	public void onStop() {
	    super.onStop();
	    // Shutdown Sphero connection view
	    mSpheroConnectionView.shutdown();

	    // Disconnect from the robot.
	    RobotProvider.getDefaultProvider().removeAllControls();
	}
}