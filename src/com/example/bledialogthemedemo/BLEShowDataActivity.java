package com.example.bledialogthemedemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class BLEShowDataActivity extends Activity {

	private Button listenBtn;
	private GridView gridView;
	private List<Map<String, Object>> listItems;
	private ImageView selectPic;
	private TextView valueTx;

	private String[] names = new String[] { "引擎转速: ", "车辆时速: ", "空气流量速率: ",
			"冷却液温度: ", "油箱压力: ", "氧传感器修正: " };
	private String[] values = new String[6];
	private String[] units = new String[] { "rpm", "km/h", "g/s", "℃", "KPa",
			"%" };

	String[] bleData = new String[6];
	String[] bleDataTest = new String[] { "7680", "31", "238", "164", "3.61",
			"45.7" };

	private boolean mConnected = false;
	private boolean isListening = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;

	public UUID mWriteCmdUuid = UUID
			.fromString("6a400002-b5a3-f393-e0a9-e50e24dcca9e");
	public UUID mRpmUuid = UUID
			.fromString("6a400003-b5a3-f393-e0a9-e50e24dcca9e");
	public UUID mSpdUuid = UUID
			.fromString("6a400004-b5a3-f393-e0a9-e50e24dcca9e");

	private byte[] startCmd_test = { 0x01, (byte) 0xff };
	private byte[] stopCmd = { (byte) 0xff, (byte) 0xff };

	/**
	 * GridView的Item的check状态表
	 */
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Boolean> checkStatus = new HashMap<Integer, Boolean>();
	{
		checkStatus.put(0, false);
		checkStatus.put(1, false);
		checkStatus.put(2, false);
		checkStatus.put(3, false);
		checkStatus.put(4, false);
		checkStatus.put(5, false);
	};

	/**
	 * 根据index检索GridView的Item的check状态
	 * 
	 * @param index
	 * @return
	 */
	private boolean checkStatLookUp(int index) {
		boolean result = checkStatus.get(index);
		return result;
	}

	/**
	 * 显示接收的数据
	 * 
	 * @param textView
	 * @param data
	 */
	private void displayData(TextView textView, String data) {
		if (data != null) {
			textView.setText(data);
		}
	}

	/**
	 * 广播接收
	 */
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (MyBLEService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				Toast.makeText(BLEShowDataActivity.this, "蓝牙已连接！",
						Toast.LENGTH_SHORT).show();
			} else if (MyBLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
				mConnected = false;
				Toast.makeText(BLEShowDataActivity.this, "蓝牙已断开！",
						Toast.LENGTH_SHORT).show();
			} else if (MyBLEService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				Toast.makeText(BLEShowDataActivity.this,
						"GATT SERVICE DISCOVERED.", Toast.LENGTH_SHORT).show();
			} else if (MyBLEService.ACTION_DATA_AVAILABLE.equals(action)) {
				// 向values中赋予获取的值
				values[0] = intent.getStringExtra(MyBLEService.EXTRA_DATA);
				values[1] = intent.getStringExtra(MyBLEService.EXTRA_DATA2);
				values[2] = intent.getStringExtra(MyBLEService.EXTRA_DATA3);
				values[3] = intent.getStringExtra(MyBLEService.EXTRA_DATA4);
				values[4] = intent.getStringExtra(MyBLEService.EXTRA_DATA5);
				values[5] = intent.getStringExtra(MyBLEService.EXTRA_DATA6);

				// 循环获取GridView中所有Item，并且显示数据
				for (int i = 0; i < values.length; i++) {
					valueTx = (TextView) gridView.getChildAt(i).findViewById(
							R.id.itemValueTx);
					if (mConnected && !checkStatLookUp(i)) {
						displayData(valueTx, values[i]);
					} else {
						valueTx.setText("--");
					}
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bledata);

		listenBtn = (Button) findViewById(R.id.listenBtn);
		listenBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 结合链接状态判断是否开始监听
				if (!isListening) {
					listenBtn.setText("停止监听");
					// writeCmd(mWriteCmdUuid, startCmd_test);
					isListening = true;
				} else {
					listenBtn.setText("开始监听");
					// writeCmd(mWriteCmdUuid, stopCmd);
					isListening = false;
				}
			}
		});

		listItems = new ArrayList<Map<String, Object>>();
		// 获取MyDeviceControlActivity传递的数据
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String data1 = bundle.getString("BLEData1");
		String data2 = bundle.getString("BLEData2");
		mConnected = bundle.getBoolean("connectStat");

		// ------------//

		// 将数据放入listItems
		for (int i = 0; i < names.length; i++) {
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("name", names[i]);
			listItem.put("value", values[i]);
			listItem.put("unit", units[i]);
			listItems.add(listItem);
		}

		gridView = (GridView) findViewById(R.id.gridView1);
		MyGridAdapter gridAdapter = new MyGridAdapter(this, listItems);
		gridView.setAdapter(gridAdapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				View v = parent.getChildAt(position);
				selectPic = (ImageView) v.findViewById(R.id.itemCheckedPic);
				valueTx = (TextView) v.findViewById(R.id.itemValueTx);

				if (!checkStatLookUp(position)) {
					Log.e("onItemClick", "选中,关闭Notif");
					selectPic.setImageDrawable(getResources().getDrawable(
							R.drawable.uncheck_mark));
					v.setBackgroundResource(R.drawable.shape_corners_all_dark);
					valueTx.setText("--");// 仅测试用
					checkStatus.put(position, true);
					// TODO 设置BLE的Notification状态
				} else {
					Log.e("onItemClick", "取消选中,打开Notif");
					selectPic.setImageDrawable(getResources().getDrawable(
							R.drawable.check_mark));
					v.setBackgroundResource(R.drawable.shape_corners_all);
					valueTx.setText(values[position]);// 仅测试用
					checkStatus.put(position, false);
					// TODO 设置BLE的Notification状态
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	/**
	 * 向指定UUID中写指令
	 * 
	 * @param uuid
	 */
	private void writeCmd(UUID uuid, byte cmd[]) {
		MyBLEService mBleService = new MyBLEService();
		BluetoothGattService service = mBleService.getSupportedGattServices()
				.get(2);
		BluetoothGattCharacteristic characteristic = service
				.getCharacteristic(uuid);
		characteristic.setValue(cmd);
		mBleService.writeCharacteristic(characteristic);
	}

	/**
	 * IntentFilter
	 * 
	 * @return
	 */
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MyBLEService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(MyBLEService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(MyBLEService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(MyBLEService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}
}
