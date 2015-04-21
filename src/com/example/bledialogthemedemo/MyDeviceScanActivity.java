package com.example.bledialogthemedemo;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MyDeviceScanActivity extends Activity {
	private static final String TAG = MyDeviceScanActivity.class
			.getSimpleName();

	private MyDeviceListAdapter mDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean isScanning;
	private Handler mHandler;

	private ListView listView;
	private TextView scanBtn;

	private static final int REQUEST_ENABLE_BT = 1;
	// 扫描周期�?10s
	private static final long SCAN_PERIOD = 10000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		
		setTitle("搜索蓝牙设备");
		mHandler = new Handler();

		listView = (ListView) findViewById(R.id.list1);
		scanBtn = (TextView) findViewById(R.id.scan_btn);

		// 判断是否支持低功耗蓝牙BLE
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_support, Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		// 初始化Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// 判断设备是否支持蓝牙
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		scanBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isScanning) {
					scanBtn.setText("开始扫描");
					mDeviceListAdapter.clear();
					scanLeDevice(true);
					isScanning = true;
				} else {
					scanBtn.setText("停止扫描");
					scanLeDevice(false);
					isScanning = false;
				}
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> list, View v, int position,
					long id) {
				final BluetoothDevice device = mDeviceListAdapter
						.getDevice(position);
				if (device == null)
					return;
				final Intent intent = new Intent();
				intent.setClass(MyDeviceScanActivity.this,
						MyDeviceControlActivity.class);
				intent.putExtra(MyDeviceControlActivity.EXTRAS_DEVICE_NAME,
						device.getName());
				intent.putExtra(MyDeviceControlActivity.EXTRAS_DEVICE_ADDRESS,
						device.getAddress());
				if (isScanning) {
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					isScanning = false;
				}
				startActivity(intent);
				Log.e(TAG,
						"--->单击设备名称，并将设备数据传入MyDeviceControlActivity，并跳转到MyDeviceControlActivity");
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Ensures Bluetooth is enabled on the device. If Bluetooth is not
		// currently enabled,
		// fire an intent to display a dialog asking the user to grant
		// permission to enable it.
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		// Initializes list view adapter.
		mDeviceListAdapter = new MyDeviceListAdapter();
		listView.setAdapter(mDeviceListAdapter);
		scanLeDevice(true);
	}

	/**
	 * onResume()的事件回调
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		mDeviceListAdapter.clear();
	}

//	/**
//	 * 开始扫描BLE设备
//	 */
//	private void scanLeDevice(final boolean enable) {
//		if (enable) {
//			// Stops scanning after a pre-defined scan period.
//			mHandler.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					isScanning = false;
//					mBluetoothAdapter.stopLeScan(mLeScanCallback);
//					// invalidateOptionsMenu();
//				}
//			}, SCAN_PERIOD);
//
//			isScanning = true;
//			mBluetoothAdapter.startLeScan(mLeScanCallback);
//
//		} else {
//			isScanning = false;
//			mBluetoothAdapter.stopLeScan(mLeScanCallback);
//		}
//		// invalidateOptionsMenu();
//	}
	
	/**
	 * 开始扫描BLE设备
	 */
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					isScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					// invalidateOptionsMenu();
					
					scanBtn.setText("开始扫描");
					setTitle("状态：停止扫描");
				}
			}, SCAN_PERIOD);

			isScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
			
			scanBtn.setText("停止扫描");
			setTitle("状态：正在扫描");
		} else {
			isScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			
			scanBtn.setText("开始扫描");
			setTitle("状态：停止扫描");
		}
		// invalidateOptionsMenu();
	}

	/**
	 * Device scan callback.
	 * 
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mDeviceListAdapter.addDevice(device);
					mDeviceListAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	private class MyDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflator;

		public MyDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = MyDeviceScanActivity.this.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView) view
						.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view
						.findViewById(R.id.device_name);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = mLeDevices.get(i);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText(R.string.unknown_device);
			viewHolder.deviceAddress.setText(device.getAddress());

			return view;
		}
	}

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}
}
