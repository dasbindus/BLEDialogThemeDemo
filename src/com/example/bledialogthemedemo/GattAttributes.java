package com.example.bledialogthemedemo;

import java.util.HashMap;

public class GattAttributes {
	/**根据实际的UUID查找对应的Service名称 */
	private static HashMap<String, String> attributes = new HashMap<String, String>();
	public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
	public static String CAR_TEST_DATA = "6a400004-b5a3-f393-e0a9-e50e24dcca9e";
	public static String RPM_DATA_UUID = "6a400003-b5a3-f393-e0a9-e50e24dcca9e";
	public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

	static {
		// Sample Services.
		attributes.put("0000180d-0000-1000-8000-00805f9b34fb",
				"Heart Rate Service");
		attributes.put("0000180a-0000-1000-8000-00805f9b34fb",
				"Device Information Service");
		attributes.put("00001800-0000-1000-8000-00805f9b34fb", "GAP Service");
		// Sample Characteristics.
		attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
		attributes.put("00002a29-0000-1000-8000-00805f9b34fb",
				"Manufacturer Name String");
	}

	public static String lookup(String uuid, String defaultName) {
		String name = attributes.get(uuid);
		return name == null ? defaultName : name;
	}
}
