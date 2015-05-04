package com.example.bledialogthemedemo;

import java.util.HashMap;

public class GattAttributes {
	/** 根据实际的UUID查找对应的Service名称 */
	private static HashMap<String, String> attributes = new HashMap<String, String>();

	/** 引擎转速的UUID */
	public static String RPM_DATA_UUID = "6a400003-b5a3-f393-e0a9-e50e24dcca9e";
	/** 车辆时速的UUID */
	public static String SPD_DATA_UUID = "6a400004-b5a3-f393-e0a9-e50e24dcca9e";
	/** 空气流量速率的UUID */
	public static String MAF_DATA_UUID = "6a400005-b5a3-f393-e0a9-e50e24dcca9e";
	/** 冷却液温度的UUID */
	public static String ECT_DATA_UUID = "6a400006-b5a3-f393-e0a9-e50e24dcca9e";
	/** 邮箱压力的UUID */
	public static String MAP_DATA_UUID = "6a400007-b5a3-f393-e0a9-e50e24dcca9e";
	/** 氧传感器修正的UUID */
	public static String O1V_DATA_UUID = "6a400008-b5a3-f393-e0a9-e50e24dcca9e";
	/** 瞬时油耗的UUID */
	public static String FCR_DATA_UUID = "6a400009-b5a3-f393-e0a9-e50e24dcca9e";
	/** 故障吗DTC的UUID */
	public static String DTC_DATA_UUID = "6a40000a-b5a3-f393-e0a9-e50e24dcca9e";
	public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

	static {
		// Sample Services.
		attributes.put("0000180d-0000-1000-8000-00805f9b34fb",
				"Heart Rate Service");
		attributes.put("0000180a-0000-1000-8000-00805f9b34fb",
				"Device Information Service");
		attributes.put("00001800-0000-1000-8000-00805f9b34fb", "GAP Service");
		// Sample Characteristics.
		attributes.put("00002a29-0000-1000-8000-00805f9b34fb",
				"Manufacturer Name String");
	}

	public static String lookup(String uuid, String defaultName) {
		String name = attributes.get(uuid);
		return name == null ? defaultName : name;
	}
}
