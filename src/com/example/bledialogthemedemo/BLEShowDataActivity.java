package com.example.bledialogthemedemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class BLEShowDataActivity extends Activity {

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bledata);

		listItems = new ArrayList<Map<String, Object>>();
		// 获取MyDeviceControlActivity传递的数据
		// Intent intent = getIntent();
		// Bundle bundle = intent.getExtras();
		// String data1 = bundle.getString("BLEData1");
		// String data2 = bundle.getString("BLEData2");
		String data1 = "rpm";
		String data2 = "spd";

		// ------向values中赋予获取的值-------//
		for (int i = 0; i < values.length; i++) {
			// values[i] = bleData[i];
			values[i] = bleDataTest[i];
		}
		// ------------//

		// 将数据放入listItems
		// Map<String, Object> listItem = new HashMap<String, Object>();
		// listItem.put("rpm", data1);
		// listItem.put("spd", data2);
		// listItems.add(listItem);

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
}
