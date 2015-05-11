package com.example.bledialogthemedemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MyDeviceControlActivity extends Activity {
	private final static String TAG = MyDeviceControlActivity.class
			.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private TextView mConnectionState;
	private TextView mDataField;
	private TextView mDataField2;
	private TextView mDataField3;
	private String mDeviceName;
	private String mDeviceAddress;
	private ExpandableListView mGattServicesList;
	private MyBLEService mBleService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";

	private boolean sendable = false;
	private boolean isListening = false;
	// private Button sendBtn;
	private Button startLisBtn;
	BluetoothGattCharacteristic characteristic;
	BluetoothGattService gattService;

	// ---补充内容---//
	public static final String UUID_KEY_DATA = "6a400002-b5a3-f393-e0a9-e50e24dcca9e";

	public UUID mWriteCmdUuid = UUID
			.fromString("6a400002-b5a3-f393-e0a9-e50e24dcca9e");
	public UUID mRpmUuid = UUID
			.fromString("6a400003-b5a3-f393-e0a9-e50e24dcca9e");
	public UUID mSpdUuid = UUID
			.fromString("6a400004-b5a3-f393-e0a9-e50e24dcca9e");

	private Handler mHandler;
	private byte[] startCmd_test = { 0x01, (byte) 0xff };
	private byte[] stopCmd = { (byte) 0xff, (byte) 0xff };
	
	
	/**
	 * 管理Service的生命周期
	 */
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBleService = ((MyBLEService.LocalBinder) service).getService();
			if (!mBleService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBleService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBleService = null;
		}
	};

	/**
	 * Handles various events fired by the Service.
	 * ACTION_GATT_CONNECTED:connected to a GATT server.
	 * 
	 * ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	 * 
	 * ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	 * 
	 * ACTION_DATA_AVAILABLE: received data from the device. This can be a
	 * result of read or notification operations.
	 * 
	 */
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (MyBLEService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				updateConnectionState(R.string.connected);
				invalidateOptionsMenu();
			} else if (MyBLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
				mConnected = false;
				updateConnectionState(R.string.disconnected);
				invalidateOptionsMenu();
				clearUI();
				startLisBtn.setText("开始监听");
			} else if (MyBLEService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				// 显示�?有支持的设备的Service和Characteristic
				displayGattServices(mBleService.getSupportedGattServices());
			} else if (MyBLEService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayData(mDataField,
						intent.getStringExtra(MyBLEService.EXTRA_DATA));
				displayData(mDataField2,
						intent.getStringExtra(MyBLEService.EXTRA_DATA2));

				// TODO
				/*
				 * 注意此处修改为Activity和Fragment之间传递数据的方式 不用Intent,
				 * intent是用在Activity之间传递数据的
				 */
				Intent bleDataIntent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putBoolean("connectStat", mConnected);
				bleDataIntent.putExtras(bundle);
				bleDataIntent.setClass(MyDeviceControlActivity.this,
						BLEShowDataActivity.class);
				startActivity(bleDataIntent);
			}
		}
	};

	// If a given GATT characteristic is selected, check for supported features.
	// This sample
	// demonstrates 'Read' and 'Notify' features. See
	// http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for
	// the complete
	// list of supported characteristic features.
	private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			if (mGattCharacteristics != null) {
				final BluetoothGattCharacteristic characteristic = mGattCharacteristics
						.get(groupPosition).get(childPosition);
				final int charaProp = characteristic.getProperties();
				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
					// If there is an active notification on a characteristic,
					// clear
					// it first so it doesn't update the data field on the user
					// interface.
					if (mNotifyCharacteristic != null) {
						mBleService.setCharacteristicNotification(
								mNotifyCharacteristic, false);
						mNotifyCharacteristic = null;
					}
					mBleService.readCharacteristic(characteristic);
				}
				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
					mNotifyCharacteristic = characteristic;
					mBleService.setCharacteristicNotification(characteristic,
							true);
					// add
					if (mNotifyCharacteristic != null) {
						mBleService.setCharacteristicNotification(
								mNotifyCharacteristic, true);
						mNotifyCharacteristic = null;
					}

					// end
				}
				return true;
			}
			return false;
		}
	};

	/**
	 * 清空UI（当断开链接时使用）
	 */
	private void clearUI() {
		mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
		mDataField.setText(R.string.no_data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gatt_service_characteristic_v2);

		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		// Sets up UI references.
		((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
		mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
		mGattServicesList.setOnChildClickListener(servicesListClickListner);
		mConnectionState = (TextView) findViewById(R.id.connection_state);
		mDataField = (TextView) findViewById(R.id.data_value);
		mDataField2 = (TextView) findViewById(R.id.data_value2);
		mDataField3 = (TextView) findViewById(R.id.data_value3);

		startLisBtn = (Button) findViewById(R.id.startListenBtn);
		startLisBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 结合链接状态判断是否开始监听
				if (!isListening) {
					startLisBtn.setText("停止监听");
					writeCmd(mWriteCmdUuid, startCmd_test);
					isListening = true;
				} else {
					startLisBtn.setText("开始监听");
					writeCmd(mWriteCmdUuid, stopCmd);
					isListening = false;
				}
			}
		});

		getActionBar().setTitle(mDeviceName);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent gattServiceIntent = new Intent(this, MyBLEService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBleService != null) {
			final boolean result = mBleService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBleService = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			mBleService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
			mBleService.disconnect();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState.setText(resourceId);
			}
		});
	}

	private void displayData(TextView textView, String data) {
		if (data != null) {
			textView.setText(data);
		}
	}

	/**
	 * 向指定UUID中写指令
	 * 
	 * @param uuid
	 */
	private void writeCmd(UUID uuid, byte cmd[]) {
		BluetoothGattService service = mBleService.getSupportedGattServices()
				.get(2);
		BluetoothGattCharacteristic characteristic = service
				.getCharacteristic(uuid);
		characteristic.setValue(cmd);
		mBleService.writeCharacteristic(characteristic);
	}

	// Demonstrates how to iterate through the supported GATT
	// Services/Characteristics.
	// In this sample, we populate the data structure that is bound to the
	// ExpandableListView
	// on the UI.
	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null)
			return;
		String uuid = null;
		String unknownServiceString = getResources().getString(
				R.string.unknown_service);
		String unknownCharaString = getResources().getString(
				R.string.unknown_characteristic);
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// 遍历所有可用的GattService
		for (BluetoothGattService gattService : gattServices) {
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();
			currentServiceData.put(LIST_NAME,
					GattAttributes.lookup(uuid, unknownServiceString));
			currentServiceData.put(LIST_UUID, uuid);
			gattServiceData.add(currentServiceData);

			// -----Service的字段信息-----//(补充内容)
			int type = gattService.getType();
			Log.d(TAG, "---->>>service type:" + BLEUtil.getServiceType(type));
			Log.d(TAG, "---->>>includedServiceSize:"
					+ gattService.getIncludedServices().size());
			Log.d(TAG, "---->>>service uuid:" + gattService.getUuid());
			// ------------------end--------------------//

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

			// 遍历所有可用的Characteristic
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();
				currentCharaData.put(LIST_NAME,
						GattAttributes.lookup(uuid, unknownCharaString));
				currentCharaData.put(LIST_UUID, uuid);
				gattCharacteristicGroupData.add(currentCharaData);

				// -----Characteristics的字段信息-----//(补充内容)
				Log.d(TAG, "---->>>char uuid:" + gattCharacteristic.getUuid());

				int permission = gattCharacteristic.getPermissions();
				Log.d(TAG,
						"---->>>char permission:"
								+ BLEUtil.getCharPermission(permission));

				int property = gattCharacteristic.getProperties();
				Log.d(TAG,
						"---->>>char property:"
								+ BLEUtil.getCharPropertie(property));

				byte[] data = gattCharacteristic.getValue();
				if (data != null && data.length > 0) {
					Log.d(TAG, "---->>>char Value:" + new String(data));
				}

				// UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
				// if (gattCharacteristic.getUuid().toString()
				// .equals(GattAttributes.RPM_DATA_UUID)
				// && sendable) {
				// mBleService.setCharacteristicNotification(
				// gattCharacteristic, true);
				//
				// mHandler.postDelayed(new Runnable() {
				// @Override
				// public void run() {
				// mBleService.readCharacteristic(gattCharacteristic);
				// }
				// }, 500);
				//
				// // 判断UUID符合后会触发mOnDataAvailable.onCharacteristicWrite()
				// mBleService.setCharacteristicNotification(
				// gattCharacteristic, true);
				// // 设置数据内容
				// gattCharacteristic.setValue("-->hello!");
				// // 向蓝牙模块写入数据
				// mBleService.writeCharacteristic(gattCharacteristic);
				// }

				// -----Descriptors的字段信息-----//
				List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic
						.getDescriptors();
				for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
					Log.d(TAG, "---->>>desc uuid:" + gattDescriptor.getUuid());
					int descPermission = gattDescriptor.getPermissions();
					Log.d(TAG,
							"---->>>desc permission:"
									+ BLEUtil.getDescPermission(descPermission));

					byte[] desData = gattDescriptor.getValue();
					if (desData != null && desData.length > 0) {
						Log.d(TAG, "---->>>desc value:" + new String(desData));
					}
				}
				// ----------------------end----------------------//

			}
			mGattCharacteristics.add(charas);
			gattCharacteristicData.add(gattCharacteristicGroupData);
		}

		SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
				this, gattServiceData,
				android.R.layout.simple_expandable_list_item_2, new String[] {
						LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
						android.R.id.text2 }, gattCharacteristicData,
				android.R.layout.simple_expandable_list_item_2, new String[] {
						LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
						android.R.id.text2 });
		mGattServicesList.setAdapter(gattServiceAdapter);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MyBLEService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(MyBLEService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(MyBLEService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(MyBLEService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

}
