package com.techjoynt.android.nxt.fragment.dialog;

import java.util.Set;

import com.techjoynt.android.nxt.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class NXTBackup extends DialogFragment {
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
    
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private BluetoothAdapter mBtAdapter;
    
    private View mView;
    
    /**
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
    	mView = inflater.inflate(R.layout.device_list, null);
    	return mView;
    }**/
    
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	super.onCreateDialog(savedInstanceState);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	
    	LayoutInflater inflater = getActivity().getLayoutInflater();
    	mView = inflater.inflate(R.layout.device_list, null);
    	
    	builder.setView(mView);
		builder.setIcon(R.drawable.ic_action_info);
		builder.setTitle(R.string.connect_to_device);
		
		return builder.create();
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	mPairedDevicesArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.device_name);
        
        ListView pairedListView = (ListView) mView.findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        
        ListView newDevicesListView = (ListView) mView.findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);
        
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);
        
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        
        boolean empty = true;
        
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if ((device.getBluetoothClass() != null) && (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT)) {
                    mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    empty = false;
                }
            }
        }
        if (!empty) {
            mView.findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.no_devices).setVisibility(View.GONE);
        }
    }
    
    @Override
	public void onDestroy() {
        super.onDestroy();
        
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        
        getActivity().unregisterReceiver(mReceiver);
    }
    
    private void doDiscovery() {
    	getActivity().setProgressBarIndeterminateVisibility(true);
        getDialog().setTitle("Scanning...");
        
        //findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        
        mBtAdapter.startDiscovery();
        
        mNewDevicesArrayAdapter.clear();
        mView.findViewById(R.id.title_new_devices).setVisibility(View.GONE);
        if (mPairedDevicesArrayAdapter.getCount() == 0) {
            mView.findViewById(R.id.no_devices).setVisibility(View.VISIBLE);
        }
    }
    
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            mBtAdapter.cancelDiscovery();
            
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            
            getTargetFragment().getActivity().setResult(Activity.RESULT_OK, intent);
            getDialog().dismiss();
        }
    };
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if ((device.getBondState() != BluetoothDevice.BOND_BONDED) && (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT)) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mView.findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.no_devices).setVisibility(View.GONE);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	getTargetFragment().getActivity().setProgressBarIndeterminateVisibility(false);
                getDialog().setTitle("Select device");
            }
        }
    };
}