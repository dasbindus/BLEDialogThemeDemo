package com.example.bledialogthemedemo;

import java.util.List;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MyBLEService extends Service {
	private final static String TAG = MyBLEService.class.getSimpleName();

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;

	private int mConnectionState = STATE_DISCONNECTED;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
	public final static String EXTRA_DATA2 = "com.example.bluetooth.le.EXTRA_DATA2";
	public final static String EXTRA_DATA3 = "com.example.bluetooth.le.EXTRA_DATA3";
	public final static String EXTRA_DATA4 = "com.example.bluetooth.le.EXTRA_DATA4";
	public final static String EXTRA_DATA5 = "com.example.bluetooth.le.EXTRA_DATA5";
	public final static String EXTRA_DATA6 = "com.example.bluetooth.le.EXTRA_DATA6";
	public final static String EXTRA_DATA7 = "com.example.bluetooth.le.EXTRA_DATA7";

	/** 引擎转速的UUID */
	public final static UUID UUID_RPM_DATA = UUID
			.fromString(GattAttributes.RPM_DATA_UUID);
	/** 车辆时速的UUID */
	public final static UUID UUID_SPD_DATA = UUID
			.fromString(GattAttributes.SPD_DATA_UUID);
	/** 空气流量速率的UUID */
	public final static UUID UUID_MAF_DATA = UUID
			.fromString(GattAttributes.MAF_DATA_UUID);
	/** 冷却液温度的UUID */
	public final static UUID UUID_ECT_DATA = UUID
			.fromString(GattAttributes.ECT_DATA_UUID);
	/** 邮箱压力的UUID */
	public final static UUID UUID_MAP_DATA = UUID
			.fromString(GattAttributes.MAP_DATA_UUID);
	/** 氧传感器修正的UUID */
	public final static UUID UUID_O1V_DATA = UUID
			.fromString(GattAttributes.O1V_DATA_UUID);
	/** 瞬时油耗的UUID */
	public final static UUID UUID_FCR_DATA = UUID
			.fromString(GattAttributes.FCR_DATA_UUID);
	/** 故障吗DTC的UUID */
	public final static UUID UUID_DTC_DATA = UUID
			.fromString(GattAttributes.DTC_DATA_UUID);

	/**
	 * GATT事件的回调函数（例如蓝牙连接状态变化以及Service的发现操作）
	 * 
	 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			String intentAction;
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED;
				mConnectionState = STATE_CONNECTED;
				broadcastUpdate(intentAction);
				Log.e(TAG, "------>连接到 GATT server.");
				// 当成功建立连接后，尝试进行discover services
				Log.e(TAG,
						"------>尝试开始 service discovery:"
								+ mBluetoothGatt.discoverServices());

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
				Log.e(TAG, "------>断开与 GATT server 的连接.");
				broadcastUpdate(intentAction);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, descriptor);
			}
		}

	};

	public class LocalBinder extends Binder {
		MyBLEService getService() {
			return MyBLEService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		close();
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	/**
	 * 读取/更新 Characteristic的value
	 * 
	 * @param action
	 * @param characteristic
	 */
	private void broadcastUpdate(final String action,
			final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);
		UUID keyUuid = characteristic.getUuid();

		// 处理RPM数据
		if (UUID_RPM_DATA.equals(keyUuid)) {
			final byte[] carData = characteristic.getValue();
			String carTestDataStr = new String(carData);
			String[] params = carTestDataStr.split(" ");
			int A = Integer.parseInt(params[0], 16);
			int B = Integer.parseInt(params[1], 16);
			// 处理接收数据为10进制（由于receiver接收String类型，所以使用"" + int）
			intent.putExtra(EXTRA_DATA, "" + (((A * 256) + B) / 4));
			System.out.println("RPM: " + carTestDataStr);
		}
		// 处理SPD数据
		if (UUID_SPD_DATA.equals(keyUuid)) {
			final byte[] carData = characteristic.getValue();
			String carTestDataStr = new String(carData);
			String[] params = carTestDataStr.split(" ");
			int A = Integer.parseInt(params[0], 16);
			intent.putExtra(EXTRA_DATA2, "" + A);
			System.out.println("SPD: " + carTestDataStr);
		}
		// 处理MAF数据
		if (UUID_MAF_DATA.equals(keyUuid)) {
			final byte[] carData = characteristic.getValue();
			String carTestDataStr = new String(carData);
			String[] params = carTestDataStr.split(" ");
			int A = Integer.parseInt(params[0], 16);
			int B = Integer.parseInt(params[1], 16);
			intent.putExtra(EXTRA_DATA3, "" + (((A * 256) + B) / 100));
			System.out.println("MAF: " + carTestDataStr);
		}
		// 处理ECT数据
		if (UUID_ECT_DATA.equals(keyUuid)) {
			final byte[] carData = characteristic.getValue();
			String carTestDataStr = new String(carData);
			String[] params = carTestDataStr.split(" ");
			int A = Integer.parseInt(params[0], 16);
			intent.putExtra(EXTRA_DATA4, "" + (A - 40));
			System.out.println("ECT: " + carTestDataStr);
		}
		// 处理MAP数据
		if (UUID_MAP_DATA.equals(keyUuid)) {
			final byte[] carData = characteristic.getValue();
			String carTestDataStr = new String(carData);
			String[] params = carTestDataStr.split(" ");
			int A = Integer.parseInt(params[0], 16);
			intent.putExtra(EXTRA_DATA5, "" + A);
			System.out.println("MAP: " + carTestDataStr);
		}
		// 处理O1V数据
		if (UUID_O1V_DATA.equals(keyUuid)) {
			final byte[] carData = characteristic.getValue();
			String carTestDataStr = new String(carData);
			String[] params = carTestDataStr.split(" ");
			int A = Integer.parseInt(params[0], 16);
			int B = Integer.parseInt(params[1], 16);
			intent.putExtra(EXTRA_DATA6, "" + ((B - 128) * 100 / 128));
			System.out.println("O1V: " + carTestDataStr);
		}
		// 处理FCR数据
		if (UUID_FCR_DATA.equals(keyUuid)) {
			final byte[] carData = characteristic.getValue();
			String carTestDataStr = new String(carData);
			String[] params = carTestDataStr.split(" ");
			int A = Integer.parseInt(params[0], 16);
			int B = Integer.parseInt(params[1], 16);
			intent.putExtra(EXTRA_DATA7, "" + (((A * 256) + B) / 20));
			System.out.println("FCR: " + carTestDataStr);
		} else {
			// 若UUID不符合，则处理为HEX, （处理后的结果不发广播！）
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(
						data.length);
				for (byte byteChar : data)
					stringBuilder.append(String.format("%02X ", byteChar));
				Log.e(TAG, (new String(data) + "\n" + stringBuilder.toString()));
			}

		}
		sendBroadcast(intent);
	}

	/**
	 * 读取/更新descriptor
	 * 
	 * @param action
	 * @param descriptor
	 */
	private void broadcastUpdate(final String action,
			final BluetoothGattDescriptor descriptor) {
		final Intent intent = new Intent(action);

		final byte[] data = descriptor.getValue();
		if (data != null && data.length > 0) {
			final StringBuilder stringBuilder = new StringBuilder(data.length);
			for (byte byteChar : data)
				stringBuilder.append(String.format("%02X ", byteChar));
			intent.putExtra(EXTRA_DATA,
					new String(data) + "\n" + stringBuilder.toString());
		}
		sendBroadcast(intent);
	}

	/**
	 * 初始化BluetoothAdapter
	 * 
	 * @return boolean类型 true:初始化成功 false:初始化失败
	 */
	public boolean initialize() {
		// 对于API>=18，通过BluetoothManager初始化BluetoothAdapter.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "------>无法初始化BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "------>无法初始化BluetoothManager.");
			return false;
		}
		Log.e(TAG, "------>初始化BluetoothManager成功.");
		return true;
	}

	/**
	 * 连接GATT Server(server在BLE设备上). 连接的结果以异步的形式通过
	 * {@code BluetoothGattCallback#onConnectionStateChange(BluetoothGatt, int, int)}
	 * 回调
	 * 
	 * @param 目的设备的地址
	 *            (address)
	 * @return 如果连接成功则返回true.
	 */
	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			Log.w(TAG, "------>BluetoothAdapter没有初始化，或者未指明address.");
			return false;
		}

		// 之前连接过的设备，尝试重新连接
		if (mBluetoothDeviceAddress != null
				&& address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.e(TAG, "------>正尝试reconnect一个之前连接过的设备.");
			if (mBluetoothGatt.connect()) {
				mConnectionState = STATE_CONNECTING;
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "------>未发现设备，无法连接.");
			return false;
		}
		// 此处为了实现直接连接到设备，故设置autoConnect参数为false
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		Log.d(TAG, "------>正尝试创建一个新的连接.");
		mBluetoothDeviceAddress = address;
		mConnectionState = STATE_CONNECTING;
		return true;
	}

	/**
	 * 断开一个已存在的连接或者取消一个尚未进行的连接。断开连接的结果以异步的形式通过
	 * {@code BluetoothGattCallback#onConnectionStateChange(BluetoothGatt, int, int)}
	 * 回调
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "------>断开连接过程:BluetoothAdapter未初始化.");
			return;
		}
		mBluetoothGatt.disconnect();
	}

	/**
	 * 彻底关闭连接，在使用完BLE设备后，app必须调用这个方法保证资源被合理的释放.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/**
	 * 读取给定的{@code BluetoothGattCharacteristic}的值. 读取的结果以异步的形式通过
	 * {@code BluetoothGattCallback#onCharacteristicRead(BluetoothGatt, BluetoothGattCharacteristic, int)}
	 * 回调
	 * 
	 * @param characteristic
	 *            指定读取的 characteristic的值
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "------>readCharacteristic过程:BluetoothAdapter未初始化.");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);

	}

	/**
	 * 针对某个给定的characteristic，决定是否进行Notification
	 * 
	 * @param characteristic
	 *            给定的characteristic
	 * @param enabled
	 *            true：使能Notification；false:不使能Notification
	 */
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		boolean notifSuc = false;// add
		notifSuc = mBluetoothGatt.setCharacteristicNotification(characteristic,
				enabled);
		Log.e(TAG, "notif:" + notifSuc);// add

		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
				.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		boolean desWriteSuc = false;// add
		desWriteSuc = mBluetoothGatt.writeDescriptor(descriptor);
		Log.e(TAG, "descriptor write sucess:" + desWriteSuc);// add

	}

	/**
	 * 写BluetoothGattCharacteristic的值
	 * 
	 * @param characteristic
	 *            指定写的 characteristic的值
	 */
	public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	/**
	 * 读取指定characteristic中的descriptor的值
	 * 
	 * @param characteristic
	 */
	public void readDescriptor(BluetoothGattDescriptor characteristic) {
		mBluetoothGatt.readDescriptor(characteristic);
	}

	/**
	 * 向descriptor里写特性值（这个特性具有notify属性）
	 * 
	 * @param descriptor
	 *            描述符
	 */
	public void writeDescriptor(BluetoothGattDescriptor descriptor) {
		mBluetoothGatt.writeDescriptor(descriptor);
	}

	/**
	 * 在已连接设备中检索支持的GATT services,并列表(list). 这个方法应该在
	 * {@code BluetoothGatt#discoverServices()}成功实现之后调用
	 * 
	 * @return 返回一个包含支持的GATT services的list
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;
		return mBluetoothGatt.getServices();
	}

}
