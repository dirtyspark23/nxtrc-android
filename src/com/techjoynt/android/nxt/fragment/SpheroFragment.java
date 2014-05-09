package com.techjoynt.android.nxt.fragment;

import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.techjoynt.android.nxt.R;

public class SpheroFragment extends Fragment {
	private Sphero mRobot;
	
	private SpheroConnectionView mSpheroConnectionView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
		return inflater.inflate(null, container, false);
	}
}