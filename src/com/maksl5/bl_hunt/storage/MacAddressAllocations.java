/**
 *  MacAdressAllocations.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt.storage;



import java.lang.reflect.Field;
import java.util.HashMap;



/**
 * 
 * Addresses from <i>IEEE STANDARDS ASSOCIATION</i>.
 * 
 * @author Maksl5[Markus Bensing]
 * 
 */
public class MacAddressAllocations {

	private static HashMap<String, String[]> manufacturerMacs = null;
	private static HashMap<String, Integer> manufacturerExps = null;

	public static HashMap<String, String[]> getHashMap() {

		if (manufacturerMacs != null) {
			if (!manufacturerMacs.isEmpty()) { return manufacturerMacs; }
		}

		HashMap<String, String[]> hsHashMap = new HashMap<String, String[]>();

		Field[] fields = MacAddressAllocations.class.getDeclaredFields();

		for (Field field : fields) {
			if (field.getType().equals(String[].class)) {
				String[] macStrings;
				try {
					macStrings = (String[]) field.get(MacAddressAllocations.class);
					hsHashMap.put(field.getName().replace("_", " "), macStrings);
				}
				catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		manufacturerMacs = hsHashMap;
		return hsHashMap;

	}

	public synchronized static String getManufacturer(String macAddress) {

		HashMap<String, String[]> manufacturers = getHashMap();

		for (String manufacturer : manufacturers.keySet()) {
			String[] macs = manufacturers.get(manufacturer);

			for (String mac : macs) {
				if (macAddress.substring(0, 8).equals(mac)) { return manufacturer; }
			}

		}

		return "Unknown";
	}

	public static int getExp(String manufacturer) {

		if (manufacturerExps != null) {
			if (!manufacturerExps.isEmpty()) { return manufacturerExps.get(manufacturer + "_exp"); }
		}

		int exp = 0;

		try {
			Field expField = MacAddressAllocations.class.getDeclaredField(manufacturer + "_exp");
			exp = expField.getInt(MacAddressAllocations.class);
		}
		catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return exp;

	}

	public static HashMap<String, Integer> getExpHashMap() {

		if (manufacturerExps != null) {
			if (!manufacturerExps.isEmpty()) { return manufacturerExps; }
		}

		HashMap<String, Integer> hsHashMap = new HashMap<String, Integer>();

		Field[] fields = MacAddressAllocations.class.getDeclaredFields();

		for (Field field : fields) {
			if (field.getType().equals(int.class)) {
				int exp;
				try {
					exp = (Integer) field.get(MacAddressAllocations.class);
					hsHashMap.put(field.getName(), exp);
				}
				catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		manufacturerExps = hsHashMap;
		return hsHashMap;
	}

	static final String[] Apple = {
									"00:03:93", "00:05:02", "00:0A:27", "00:0A:95", "00:0D:93",
									"00:10:FA", "00:11:24", "00:14:51", "00:16:CB", "00:17:F2",
									"00:19:E3", "00:1B:63", "00:1C:B3", "00:1D:4F", "00:1E:52",
									"00:1E:C2", "00:1F:5B", "00:1F:F3", "00:21:E9", "00:22:41",
									"00:23:12", "00:23:32", "00:23:6C", "00:23:DF", "00:24:36",
									"00:25:00", "00:25:4B", "00:25:BC", "00:26:08", "00:26:4A",
									"00:26:B0", "00:26:BB", "00:30:65", "00:3E:E1", "00:50:E4",
									"00:A0:3F", "00:A0:40", "00:C6:10", "00:F4:B9", "04:0C:CE",
									"04:1E:64", "04:54:53", "04:F7:E4", "08:00:07", "0C:74:C2",
									"0C:77:1A", "10:40:F3", "10:93:E9", "10:9A:DD", "10:DD:B1",
									"14:10:9F", "14:5A:05", "14:8F:C6", "18:20:32", "18:34:51",
									"18:9E:FC", "18:E7:F4", "1C:AB:A7", "20:C9:D0", "24:AB:81",
									"28:37:37", "28:6A:B8", "28:6A:BA", "28:CF:DA", "28:E0:2C",
									"28:E7:CF", "34:15:9E", "34:51:C9", "3C:07:54", "3C:D0:F8",
									"40:30:04", "40:3C:FC", "40:6C:8F", "40:A6:D9", "40:D3:2D",
									"44:2A:60", "44:4C:0C", "44:D8:84", "48:60:BC", "4C:B1:99",
									"50:EA:D6", "54:26:96", "58:1F:AA", "58:55:CA", "58:B0:35",
									"5C:59:48", "5C:95:AE", "60:33:4B", "60:C5:47", "60:FA:CD",
									"60:FB:42", "64:20:0C", "64:B9:E8", "64:E6:82", "68:09:27",
									"68:96:7B", "68:A8:6D", "6C:3E:6D", "6C:C2:6B", "70:56:81",
									"70:73:CB", "70:CD:60", "70:DE:E2", "74:E1:B6", "74:E2:F5",
									"78:A3:E4", "78:CA:39", "7C:11:BE", "7C:6D:62", "7C:C3:A1",
									"7C:C5:37", "7C:D1:C3", "7C:F0:5F", "80:49:71", "80:92:9F",
									"84:29:99", "84:85:06", "84:FC:FE", "88:53:95", "88:C6:63",
									"8C:2D:AA", "8C:58:77", "8C:7B:9D", "8C:FA:BA", "90:27:E4",
									"90:84:0D", "98:03:D8", "98:B8:E3", "98:D6:BB", "98:FE:94",
									"9C:20:7B", "A4:67:06", "A4:B1:97", "A4:D1:D2", "A8:20:66",
									"B0:65:BD", "B4:F0:AB", "B8:17:C2", "B8:8D:12", "B8:C7:5D",
									"B8:F6:B1", "B8:FF:61", "BC:52:B7", "BC:67:78", "C0:84:7A",
									"C0:9F:42", "C4:2C:03", "C8:2A:14", "C8:33:4B", "C8:BC:C8",
									"CC:08:E0", "D0:23:DB", "D4:9A:20", "D8:00:4D", "D8:30:62",
									"D8:9E:3F", "D8:A2:5E", "D8:D1:CB", "DC:2B:61", "E0:B9:BA",
									"E0:C9:7A", "E0:F8:47", "E4:8B:7F", "E4:CE:8F", "E8:04:0B",
									"E8:06:88", "EC:85:2F", "F0:B4:79", "F0:CB:A1", "F0:DC:E2",
									"F8:1E:DF", "FC:25:3F" };

	static final String[] Nokia = {
									"00:02:EE", "00:0B:E1", "00:0E:ED", "00:0F:BB", "00:10:B3",
									"00:11:9F", "00:12:62", "00:13:70", "00:13:FD", "00:14:A7",
									"00:15:2A", "00:15:A0", "00:15:DE", "00:16:4E", "00:16:BC",
									"00:17:4B", "00:17:B0", "00:18:0F", "00:18:42", "00:18:8D",
									"00:18:C5", "00:19:2D", "00:19:4F", "00:19:79", "00:19:B7",
									"00:1A:16", "00:1A:89", "00:1A:DC", "00:1B:33", "00:1B:AF",
									"00:1B:EE", "00:1C:35", "00:1C:9A", "00:1C:D4", "00:1C:D6",
									"00:1D:3B", "00:1D:6E", "00:1D:98", "00:1D:E9", "00:1D:FD",
									"00:1E:3A", "00:1E:3B", "00:1E:A3", "00:1E:A4", "00:1F:00",
									"00:1F:01", "00:1F:5C", "00:1F:5D", "00:1F:DE", "00:1F:DF",
									"00:21:08", "00:21:09", "00:21:AA", "00:21:AB", "00:21:FC",
									"00:21:FE", "00:22:65", "00:22:66", "00:22:FC", "00:22:FD",
									"00:23:B4", "00:24:03", "00:24:04", "00:24:7C", "00:24:7D",
									"00:25:47", "00:25:48", "00:25:CF", "00:25:D0", "00:26:68",
									"00:26:69", "00:26:CC", "00:40:43", "00:BD:3A", "00:E0:03",
									"04:5A:95", "04:A8:2A", "0C:C6:6A", "0C:DD:EF", "10:F9:EE",
									"14:36:05", "18:14:56", "18:86:AC", "20:D6:07", "28:D1:AF",
									"2C:D2:E7", "30:38:55", "34:7E:39", "34:C8:03", "38:19:2F",
									"3C:36:3D", "3C:F7:2A", "48:DC:FB", "50:2D:1D", "5C:57:C8",
									"6C:9B:02", "6C:A7:80", "6C:E9:07", "78:2E:EF", "78:CA:04",
									"80:50:1B", "90:CF:15", "94:00:70", "94:20:53", "94:3A:F0",
									"9C:18:74", "9C:4A:7B", "9C:CA:D9", "A0:4E:04", "A0:71:A9",
									"A0:F4:19", "A4:E7:31", "A8:7B:39", "A8:7E:33", "A8:E0:18",
									"AC:81:F3", "AC:93:2F", "B0:35:8D", "B0:5C:E5", "C0:38:F9",
									"C8:3D:97", "C8:97:9F", "C8:DF:7C", "D0:DB:32", "D4:5D:42",
									"D4:C1:FC", "D4:CB:AF", "D8:2A:7E", "D8:75:33", "DC:9F:A4",
									"E0:A6:70", "E4:EC:10", "E8:CB:A1", "EC:9B:5B", "F4:8E:09",
									"F8:5F:2A", "FC:E5:57" };

	static final String[] Samsung = {
										"00:00:F0", "00:02:78", "00:07:AB", "00:12:47", "00:12:FB",
										"00:13:77", "00:15:99", "00:15:B9", "00:16:32", "00:16:6B",
										"00:16:6C", "00:16:DB", "00:17:C9", "00:17:D5", "00:18:AF",
										"00:1A:8A", "00:1B:98", "00:1C:43", "00:1D:25", "00:1D:F6",
										"00:1E:7D", "00:1E:E1", "00:1E:E2", "00:1F:CC", "00:1F:CD",
										"00:21:19", "00:21:4C", "00:21:D1", "00:21:D2", "00:23:39",
										"00:23:3A", "00:23:99", "00:23:C2", "00:23:D6", "00:23:D7",
										"00:24:54", "00:24:90", "00:24:91", "00:24:E9", "00:25:38",
										"00:25:66", "00:25:67", "00:26:37", "00:26:5D", "00:26:5F",
										"00:E0:64", "04:18:0F", "04:FE:31", "08:D4:2B", "0C:71:5D",
										"0C:DF:A4", "10:1D:C0", "18:3F:47", "18:46:17", "18:E2:C2",
										"1C:62:B8", "1C:66:AA", "20:13:E0", "20:64:32", "28:98:7B",
										"2C:44:01", "34:C3:AC", "38:0A:94", "38:16:D1", "38:AA:3C",
										"38:EC:E4", "3C:5A:37", "3C:62:00", "3C:8B:FE", "44:4E:1A",
										"44:F4:59", "48:44:F7", "4C:BC:A5", "50:01:BB", "50:B7:C3",
										"50:CC:F8", "54:92:BE", "54:9B:12", "58:C3:8B", "5C:0A:5B",
										"5C:E8:EB", "60:6B:BD", "60:A1:0A", "60:D0:A9", "68:EB:AE",
										"6C:83:36", "70:F9:27", "74:45:8A", "78:25:AD", "78:47:1D",
										"78:59:5E", "78:D6:F0", "84:0B:2D", "84:25:DB", "8C:71:F8",
										"8C:77:12", "8C:C8:CD", "90:18:7C", "94:51:03", "94:63:D1",
										"98:0C:82", "98:52:B1", "9C:02:98", "A0:07:98", "A0:0B:BA",
										"A0:21:95", "A0:75:91", "A8:F2:74", "B0:D0:9C", "B0:EC:71",
										"B4:07:F9", "B4:62:93", "B8:C6:8E", "B8:D9:CE", "BC:20:A4",
										"BC:47:60", "BC:85:1F", "BC:B1:F3", "C4:73:1E", "C8:19:F7",
										"C8:7E:75", "CC:05:1B", "CC:F9:E8", "CC:FE:3C", "D0:17:6A",
										"D0:66:7B", "D0:C1:B1", "D0:DF:C7", "D4:87:D8", "D4:88:90",
										"D4:E8:B2", "D8:57:EF", "DC:71:44", "E4:7C:F9", "E4:B0:21",
										"E4:E0:C5", "E8:03:9A", "E8:11:32", "E8:E5:D6", "EC:E0:9B",
										"F0:08:F1", "F0:E7:7E", "F4:9F:54", "F4:D9:FB", "F8:D0:BD",
										"FC:A1:3E", "FC:C7:34" };

	static final String[] LG = {
								"00:1C:62", "00:1E:75", "00:1F:6B", "00:1F:E3", "00:21:FB",
								"00:22:A9", "00:24:83", "00:25:E5", "00:26:E2", "00:AA:70",
								"00:E0:91", "10:F9:6F", "20:21:A5", "3C:BD:D8", "6C:D6:8A",
								"70:05:14", "74:A7:22", "A8:16:B2", "A8:92:2C", "C0:41:F6",
								"E8:5B:5B", "E8:92:A4", "F0:1C:13", "F8:0C:F3", "5C:17:D3",
								"64:99:5D", "8C:54:1D", "B0:89:91" };

	static final String[] HTC = {
									"00:09:2D", "00:23:76", "18:87:96", "1C:B0:94", "38:E7:D8",
									"64:A7:69", "7C:61:93", "90:21:55", "A0:F4:50", "A8:26:D9",
									"BC:CF:CC", "D4:20:6D", "D8:B3:77", "E8:99:C4", "F8:DB:7F" };

	static final String[] Motorola = {
										"00:03:E0", "00:04:BD", "00:08:0E", "00:0A:28", "00:0B:06",
										"00:0C:E5", "00:0E:5C", "00:0E:C7", "00:0F:9F", "00:11:1A",
										"00:11:80", "00:11:AE", "00:12:25", "00:12:8A", "00:12:C9",
										"00:13:71", "00:14:04", "00:14:9A", "00:14:E8", "00:15:2F",
										"00:15:70", "00:15:9A", "00:15:A8", "00:16:26", "00:16:75",
										"00:16:B4", "00:16:B5", "00:17:00", "00:17:84", "00:17:E2",
										"00:17:EE", "00:18:A4", "00:18:C0", "00:19:2C", "00:19:5E",
										"00:19:A6", "00:19:C0", "00:1A:1B", "00:1A:66", "00:1A:77",
										"00:1A:AD", "00:1A:DB", "00:1A:DE", "00:1B:52", "00:1B:DD",
										"00:1C:11", "00:1C:12", "00:1C:C1", "00:1C:FB", "00:1D:6B",
										"00:1D:BE", "00:1E:46", "00:1E:5A", "00:1E:8D", "00:1F:7E",
										"00:1F:C4", "00:20:40", "00:20:75", "00:21:1E", "00:21:36",
										"00:21:43", "00:21:80", "00:22:10", "00:22:B4", "00:23:0B",
										"00:23:68", "00:23:74", "00:23:75", "00:23:95", "00:23:A2",
										"00:23:A3", "00:23:AF", "00:23:ED", "00:23:EE", "00:24:37",
										"00:24:92", "00:24:93", "00:24:95", "00:24:A0", "00:24:A1",
										"00:24:C1", "00:25:F1", "00:25:F2", "00:26:36", "00:26:41",
										"00:26:42", "00:26:BA", "00:50:E3", "00:90:9C", "00:A0:BF",
										"00:D0:88", "00:E0:0C", "00:E0:6F", "14:5B:D1", "1C:14:48",
										"20:E5:64", "2C:9E:5F", "38:6B:BB", "3C:43:8E", "3C:75:4A",
										"40:83:DE", "40:B7:F3", "40:FC:89", "48:2C:EA", "4C:CC:34",
										"5C:0E:8B", "64:ED:57", "6C:C1:D2", "70:7E:43", "74:56:12",
										"74:E7:C6", "74:F6:12", "7C:BF:B1", "80:96:B1", "84:24:8D",
										"90:B1:34", "94:CC:B9", "98:4B:4A", "A4:7A:A4", "A4:ED:4E",
										"B0:77:AC", "B0:79:94", "B4:C7:99", "C4:7D:CC", "C8:AA:21",
										"CC:7D:37", "DC:45:17", "E0:75:7D", "E4:64:49", "E4:83:99",
										"E8:6D:52", "F8:0B:BE", "F8:7B:7A", "FC:0A:81" };

	static final String[] Sony_Mobile_Device = {
												"20:54:76", "D0:51:62" };

	static final String[] Sony_Computer = {
											"00:01:4A", "00:04:1F", "00:13:15", "00:13:A9",
											"00:15:C1", "00:19:C5", "00:1A:80", "00:1D:0D",
											"00:1D:BA", "00:1F:A7", "00:22:A6", "00:24:8D",
											"00:24:BE", "08:00:46", "28:0D:FC", "30:F9:ED",
											"3C:07:71", "54:42:49", "54:53:ED", "78:84:3C",
											"A8:E3:EE", "F0:BF:97", "F8:D0:AC", "FC:0F:E6" };

	static final String[] Sony_Ericsson = {
											"00:0A:D9", "00:0E:07", "00:0F:DE", "00:12:EE",
											"00:16:20", "00:16:B8", "00:18:13", "00:19:63",
											"00:1A:75", "00:1B:59", "00:1C:A4", "00:1D:28",
											"00:1E:45", "00:1E:DC", "00:1F:E4", "00:21:9E",
											"00:22:98", "00:23:45", "00:23:F1", "00:24:EF",
											"00:25:E7", "24:21:AB", "30:17:C8", "30:39:26",
											"40:2B:A1", "58:17:0C", "5C:B5:24", "6C:0E:0D",
											"6C:23:B9", "84:00:D2", "8C:64:22", "90:C1:15",
											"B8:F9:34" };

	static final String[] BlackBerry = {
										"00:0F:86", "00:1C:CC", "00:25:57", "00:26:FF", "1C:69:A5",
										"34:BB:1F", "40:6F:2A", "68:ED:43", "70:AA:B2", "F4:0B:93",
										"00:23:7A", "14:74:11", "2C:A8:35", "30:69:4B", "30:7C:30",
										"3C:74:37", "40:5F:BE", "40:6A:AB", "70:D4:F2", "80:60:07",
										"A0:6C:EC", "A8:6A:6F", "CC:55:AD", "E8:3E:B6", "00:21:06",
										"00:24:9F" };

	static final String[] Acer = {
									"00:01:24", "00:00:E2", "00:A0:60" };

	static final String[] Huawei = {
									"00:18:82", "00:25:9D", "00:25:9E", "00:46:4B", "00:E0:FC",
									"08:19:A6", "0C:37:DC", "10:1B:54", "10:47:80", "10:C6:1F",
									"20:F3:A3", "28:3C:E4", "28:6E:D4", "34:6B:D3", "4C:1F:CC",
									"4C:B1:6C", "54:89:98", "60:DE:44", "70:72:3C", "70:7B:E8",
									"78:1D:BA", "78:F5:FD", "80:B6:86", "80:FB:06", "88:53:D4",
									"A4:99:47", "AC:DE:48", "AC:E2:15", "AC:E8:7B", "C8:D1:5E",
									"CC:CC:81", "DC:D2:FC", "E0:24:7F", "EC:23:3D", "F4:55:9C",
									"F8:3D:FF", "FC:48:EF", "04:C0:6F", "1C:1D:67", "20:2B:C1",
									"24:DB:AC", "28:5F:DB", "30:87:30", "40:4D:8E", "4C:54:99",
									"54:A5:1B", "5C:4C:A9", "84:A8:E4", "BC:76:70", "CC:96:A0",
									"F4:C7:14" };

	static final String[] ZTE = {
									"00:15:EB", "00:19:C6", "00:1E:73", "00:22:93", "00:25:12",
									"00:26:ED", "08:18:1A", "34:4B:50", "34:E0:CF", "38:46:08",
									"48:28:2F", "4C:09:B4", "4C:AC:0A", "68:1A:B2", "84:74:2A",
									"8C:E0:81", "98:F5:37", "9C:D2:4B", "B0:75:D5", "B4:B3:62",
									"C8:64:C7", "C8:7B:5B", "D0:15:4A", "DC:02:8E", "F4:6D:E2",
									"FC:C8:97" };

	static final String[] Panasonic = {
										"00:19:87", "D8:B1:2A" };

	static final String[] Asus = {
									"00:0C:6E", "00:0E:A6", "00:11:2F", "00:11:D8", "00:13:D4",
									"00:15:F2", "00:17:31", "00:18:F3", "00:1A:92", "00:1B:FC",
									"00:1D:60", "00:1E:8C", "00:1F:C6", "00:22:15", "00:23:54",
									"00:24:8C", "00:26:18", "00:E0:18", "10:BF:48", "14:DA:E9",
									"20:CF:30", "30:85:A9", "48:5B:39", "50:46:5D", "54:04:A6",
									"90:E6:BA", "BC:AE:C5", "C8:60:00", "E0:CB:1D", "E0:CB:4E",
									"F4:6D:04" };

	static final String[] Google = {
									"00:1A:11", "F8:8F:CA" };

	static final String[] Toshiba = { "00:00:39" };

	static final String[] HP = {
								"00:01:E6", "00:01:E7", "00:02:A5", "00:04:EA", "00:08:02",
								"00:08:83", "00:08:C7", "00:09:FB", "00:0A:57", "00:0B:CD",
								"00:0D:9D", "00:0E:7F", "00:0E:B3", "00:0F:20", "00:0F:61",
								"00:10:83", "00:10:E3", "00:11:0A", "00:11:85", "00:12:79",
								"00:13:21", "00:14:38", "00:14:C2", "00:15:60", "00:16:35",
								"00:17:08", "00:17:A4", "00:18:71", "00:18:FE", "00:19:BB",
								"00:1A:4B", "00:1B:78", "00:1C:C4", "00:1E:0B", "00:1F:29",
								"00:21:5A", "00:22:64", "00:23:7D", "00:24:81", "00:25:B3",
								"00:26:55", "00:30:C1", "00:50:8B", "00:60:B0", "00:80:5F",
								"00:9C:02", "10:1F:74", "18:A9:05", "1C:C1:DE", "2C:27:D7",
								"2C:41:38", "2C:76:8A", "3C:4A:92", "3C:D9:2B", "44:1E:A1",
								"64:31:50", "68:B5:99", "78:AC:C0", "78:E3:B5", "78:E7:D1",
								"98:4B:E1", "9C:8E:99", "B4:99:BA", "B8:AF:67", "D4:85:64",
								"D8:D3:85", "F4:CE:46" };

	static final String[] Gigabyte = {
										"00:0D:61", "00:0F:EA", "00:16:E6", "00:1A:4D", "00:1D:7D",
										"00:1F:D0", "00:20:ED", "00:24:1D", "1C:6F:65", "50:E5:49",
										"6C:F0:49", "90:2B:34" };

	static final String[] TomTom = {
									"00:13:6C", "00:21:3E" };

	static final String[] Siemens = {
										"00:01:E3", "00:0B:A3", "00:0D:41", "00:0E:8C", "00:19:28",
										"00:1B:1B", "00:1F:F8", "00:23:41", "08:00:06", "40:EC:F8",
										"78:9F:87", "88:4B:39" };

	static final String[] Qcom_Bluetooth_Module = { "00:0D:F0" };
	static final String[] UGSI_Bluetooth_Module =
			{
				"00:10:C6", "00:16:41", "00:1A:6B", "00:1E:37", "00:21:86", "00:24:7E", "00:27:13",
				"40:2C:F4", "70:F3:95", "CC:52:AF", "E0:2A:82", "FC:4D:D4" };
	static final String[] LiteOn_Bluetooth_Module = {
														"00:22:5F", "1C:65:9D", "20:68:9D",
														"44:6D:57", "68:A3:C4", "70:F1:A1",
														"74:DE:2B", "74:E5:43", "9C:B7:0D",
														"D0:DF:9A" };

	static final String[] Texas_Instruments = {
												"00:12:37", "00:12:4B", "00:12:D1", "00:12:D2",
												"00:17:83", "00:17:E3", "00:17:E4", "00:17:E5",
												"00:17:E6", "00:17:E7", "00:17:E8", "00:17:E9",
												"00:17:EA", "00:17:EB", "00:17:EC", "00:18:2F",
												"00:18:30", "00:18:31", "00:18:32", "00:18:33",
												"00:18:34", "00:1A:B6", "00:21:BA", "00:22:A5",
												"00:23:D4", "00:24:BA", "04:E4:51", "08:00:28",
												"10:2E:AF", "1C:45:93", "1C:E2:CC", "34:B1:F7",
												"3C:2D:B7", "3C:7D:B1", "40:5F:C2", "40:98:4E",
												"44:C1:5C", "50:56:63", "5C:6B:32", "64:7B:D4",
												"64:9C:8E", "78:C5:E5", "78:DE:E4", "7C:8E:E4",
												"84:7E:40", "90:59:AF", "90:D7:EB", "94:88:54",
												"98:59:45", "A8:63:F2", "B4:EE:D4", "B8:FF:FE",
												"BC:0D:A5", "BC:6A:29", "C0:E4:22", "C4:ED:BA",
												"C8:3E:99", "C8:A0:30", "CC:8C:E3", "D0:07:90",
												"D0:37:61", "D0:8C:B5", "D4:94:A1", "D8:54:3A",
												"D8:95:2F", "E0:C7:9D", "E0:D7:BA", "F4:FC:32" };

	static final String[] Foxconn = {
										"00:14:A4", "00:16:CE", "00:16:CF", "00:19:7D", "00:19:7E",
										"00:1C:25", "00:1C:26", "00:1D:D9", "00:1E:4C", "00:1F:3A",
										"00:1F:E1", "00:1F:E2", "00:22:68", "00:22:69", "00:23:4D",
										"00:23:4E", "00:24:2B", "00:24:2C", "00:25:56", "00:26:5C",
										"00:26:5E", "08:3E:8E", "08:ED:B9", "0C:60:76", "0C:EE:E6",
										"18:F4:6A", "1C:3E:84", "1C:66:6D", "2C:81:58", "30:0E:D5",
										"38:59:F9", "44:37:E6", "4C:0F:6E", "50:63:13", "5C:6D:20",
										"5C:AC:4C", "60:D8:19", "60:F4:94", "64:27:37", "68:94:23",
										"78:DD:08", "78:E4:00", "7C:E9:D3", "84:4B:F5", "88:9F:FA",
										"8C:7C:B5", "90:00:4E", "90:34:FC", "90:4C:E5", "90:6E:BB",
										"90:FB:A6", "94:39:E5", "9C:2A:70", "A4:17:31", "B8:76:3F",
										"C0:14:3D", "C0:18:85", "C0:CB:38", "C0:F8:DA", "C4:17:FE",
										"C4:46:19", "CC:AF:78", "D0:27:88", "D8:79:88", "E0:06:E6",
										"E4:D5:3D", "EC:55:F9", "F0:7B:CB", "F0:F0:02", "F4:B7:E2",
										"F8:2F:A8", "00:01:6C", "00:15:58", "98:E7:9A" };

	static final String[] Temic_SDS = { "00:0E:9F" };

	static final String[] Atech_Technology = { "00:19:0E" };

	static final String[] Garmin = {
									"00:05:4F", "10:C6:FC" };

	static final String[] Cisco = {
									"00:00:0C", "00:01:42", "00:01:43", "00:01:63", "00:01:64",
									"00:01:96", "00:01:97", "00:01:C7", "00:01:C9", "00:02:16",
									"00:02:17", "00:02:3D", "00:02:4A", "00:02:4B", "00:02:7D",
									"00:02:7E", "00:02:B9", "00:02:BA", "00:02:FC", "00:02:FD",
									"00:03:31", "00:03:32", "00:03:6B", "00:03:6C", "00:03:9F",
									"00:03:A0", "00:03:E3", "00:03:E4", "00:03:FD", "00:03:FE",
									"00:04:27", "00:04:28", "00:04:4D", "00:04:4E", "00:04:6D",
									"00:04:6E", "00:04:9A", "00:04:9B", "00:04:C0", "00:04:C1",
									"00:04:DD", "00:04:DE", "00:05:00", "00:05:01", "00:05:31",
									"00:05:32", "00:05:5E", "00:05:5F", "00:05:73", "00:05:74",
									"00:05:9A", "00:05:9B", "00:05:DC", "00:05:DD", "00:06:28",
									"00:06:2A", "00:06:52", "00:06:53", "00:06:7C", "00:06:C1",
									"00:06:D6", "00:06:D7", "00:06:F6", "00:07:0D", "00:07:0E",
									"00:07:4F", "00:07:50", "00:07:7D", "00:07:84", "00:07:85",
									"00:07:B3", "00:07:B4", "00:07:EB", "00:07:EC", "00:08:20",
									"00:08:21", "00:08:2F", "00:08:30", "00:08:31", "00:08:7C",
									"00:08:7D", "00:08:A3", "00:08:A4", "00:08:C2", "00:08:E2",
									"00:08:E3", "00:09:11", "00:09:12", "00:09:43", "00:09:44",
									"00:09:7B", "00:09:7C", "00:09:B6", "00:09:B7", "00:09:E8",
									"00:09:E9", "00:0A:41", "00:0A:42", "00:0A:8A", "00:0A:8B",
									"00:0A:B7", "00:0A:B8", "00:0A:F3", "00:0A:F4", "00:0B:45",
									"00:0B:46", "00:0B:5F", "00:0B:60", "00:0B:85", "00:0B:BE",
									"00:0B:BF", "00:0B:FC", "00:0B:FD", "00:0C:30", "00:0C:31",
									"00:0C:85", "00:0C:86", "00:0C:CE", "00:0C:CF", "00:0D:28",
									"00:0D:29", "00:0D:65", "00:0D:66", "00:0D:BC", "00:0D:BD",
									"00:0D:EC", "00:0D:ED", "00:0E:38", "00:0E:39", "00:0E:83",
									"00:0E:84", "00:0E:D6", "00:0E:D7", "00:0F:23", "00:0F:24",
									"00:0F:34", "00:0F:35", "00:0F:8F", "00:0F:90", "00:0F:F7",
									"00:0F:F8", "00:10:07", "00:10:0B", "00:10:0D", "00:10:11",
									"00:10:14", "00:10:1F", "00:10:29", "00:10:2F", "00:10:54",
									"00:10:79", "00:10:7B", "00:10:A6", "00:10:F6", "00:10:FF",
									"00:11:20", "00:11:21", "00:11:5C", "00:11:5D", "00:11:92",
									"00:11:93", "00:11:BB", "00:11:BC", "00:12:00", "00:12:01",
									"00:12:43", "00:12:44", "00:12:7F", "00:12:80", "00:12:D9",
									"00:12:DA", "00:13:19", "00:13:1A", "00:13:5F", "00:13:60",
									"00:13:7F", "00:13:80", "00:13:C3", "00:13:C4", "00:14:1B",
									"00:14:1C", "00:14:69", "00:14:6A", "00:14:A8", "00:14:A9",
									"00:14:F1", "00:14:F2", "00:15:2B", "00:15:2C", "00:15:62",
									"00:15:63", "00:15:C6", "00:15:C7", "00:15:F9", "00:15:FA",
									"00:16:46", "00:16:47", "00:16:9C", "00:16:9D", "00:16:C7",
									"00:16:C8", "00:17:0E", "00:17:0F", "00:17:3B", "00:17:59",
									"00:17:5A", "00:17:94", "00:17:95", "00:17:DF", "00:17:E0",
									"00:18:18", "00:18:19", "00:18:73", "00:18:74", "00:18:B9",
									"00:18:BA", "00:19:06", "00:19:07", "00:19:2F", "00:19:30",
									"00:19:55", "00:19:56", "00:19:A9", "00:19:AA", "00:19:E7",
									"00:19:E8", "00:1A:2F", "00:1A:30", "00:1A:6C", "00:1A:6D",
									"00:1A:A1", "00:1A:A2", "00:1A:E2", "00:1A:E3", "00:1B:0C",
									"00:1B:0D", "00:1B:2A", "00:1B:2B", "00:1B:53", "00:1B:54",
									"00:1B:8F", "00:1B:90", "00:1B:D4", "00:1B:D5", "00:1C:0E",
									"00:1C:0F", "00:1C:57", "00:1C:58", "00:1C:B0", "00:1C:B1",
									"00:1C:F6", "00:1C:F9", "00:1D:45", "00:1D:46", "00:1D:70",
									"00:1D:71", "00:1D:A1", "00:1D:A2", "00:1D:E5", "00:1D:E6",
									"00:1E:13", "00:1E:14", "00:1E:49", "00:1E:4A", "00:1E:79",
									"00:1E:7A", "00:1E:BD", "00:1E:BE", "00:1E:F6", "00:1E:F7",
									"00:1F:26", "00:1F:27", "00:1F:6C", "00:1F:6D", "00:1F:9D",
									"00:1F:9E", "00:1F:C9", "00:1F:CA", "00:21:1B", "00:21:1C",
									"00:21:55", "00:21:56", "00:21:A0", "00:21:A1", "00:21:D7",
									"00:21:D8", "00:22:0C", "00:22:0D", "00:22:55", "00:22:56",
									"00:22:90", "00:22:91", "00:22:BD", "00:22:BE", "00:23:04",
									"00:23:05", "00:23:33", "00:23:34", "00:23:5D", "00:23:5E",
									"00:23:AB", "00:23:AC", "00:23:EA", "00:23:EB", "00:24:13",
									"00:24:14", "00:24:50", "00:24:51", "00:24:97", "00:24:98",
									"00:24:C3", "00:24:C4", "00:24:F7", "00:24:F9", "00:25:45",
									"00:25:46", "00:25:83", "00:25:84", "00:25:B4", "00:25:B5",
									"00:26:0A", "00:26:0B", "00:26:51", "00:26:52", "00:26:98",
									"00:26:99", "00:26:CA", "00:26:CB", "00:27:0C", "00:27:0D",
									"00:2A:6A", "00:30:19", "00:30:24", "00:30:40", "00:30:71",
									"00:30:78", "00:30:7B", "00:30:80", "00:30:85", "00:30:94",
									"00:30:96", "00:30:A3", "00:30:B6", "00:30:F2", "00:3A:98",
									"00:3A:99", "00:3A:9A", "00:3A:9B", "00:3A:9C", "00:40:0B",
									"00:40:96", "00:50:0B", "00:50:0F", "00:50:14", "00:50:2A",
									"00:50:3E", "00:50:50", "00:50:53", "00:50:54", "00:50:73",
									"00:50:80", "00:50:A2", "00:50:A7", "00:50:BD", "00:50:D1",
									"00:50:E2", "00:50:F0", "00:60:09", "00:60:2F", "00:60:3E",
									"00:60:47", "00:60:5C", "00:60:70", "00:60:83", "00:64:40",
									"00:90:0C", "00:90:21", "00:90:2B", "00:90:5F", "00:90:6D",
									"00:90:6F", "00:90:86", "00:90:92", "00:90:A6", "00:90:AB",
									"00:90:B1", "00:90:BF", "00:90:D9", "00:90:F2", "00:B0:4A",
									"00:B0:64", "00:B0:8E", "00:B0:C2", "00:D0:06", "00:D0:58",
									"00:D0:63", "00:D0:79", "00:D0:90", "00:D0:97", "00:D0:BA",
									"00:D0:BB", "00:D0:BC", "00:D0:C0", "00:D0:D3", "00:D0:E4",
									"00:D0:FF", "00:DE:FB", "00:E0:14", "00:E0:1E", "00:E0:34",
									"00:E0:4F", "00:E0:8F", "00:E0:A3", "00:E0:B0", "00:E0:F7",
									"00:E0:F9", "00:E0:FE", "04:C5:A4", "04:FE:7F", "08:17:35",
									"08:1F:F3", "08:D0:9F", "0C:85:25", "0C:D9:96", "10:8C:CF",
									"10:BD:18", "18:33:9D", "18:EF:63", "1C:17:D3", "1C:AA:07",
									"1C:DF:0F", "20:37:06", "24:B6:57", "28:93:FE", "28:94:0F",
									"2C:36:F8", "2C:3F:38", "2C:54:2D", "30:37:A6", "30:E4:DB",
									"30:F7:0D", "34:BD:C8", "3C:CE:73", "3C:DF:1E", "40:55:39",
									"40:F4:EC", "44:2B:03", "44:D3:CA", "44:E4:D9", "50:3D:E5",
									"50:57:A8", "54:75:D0", "54:7F:EE", "58:35:D9", "58:8D:09",
									"58:BC:27", "58:BF:EA", "5C:50:15", "64:00:F1", "64:16:8D",
									"64:9E:F3", "64:A0:E7", "64:AE:0C", "64:D8:14", "64:D9:89",
									"68:BC:0C", "68:BD:AB", "68:EF:BD", "6C:50:4D", "6C:9C:ED",
									"70:81:05", "70:CA:9B", "88:43:E1", "88:F0:77", "8C:60:4F",
									"8C:B6:4F", "9C:4E:20", "9C:AF:CA", "A0:CF:5B", "A4:0C:C3",
									"A4:18:75", "A4:4C:11", "A4:56:30", "A4:93:4C", "A8:B1:D4",
									"AC:A0:16", "B4:14:89", "B4:A4:E3", "B8:62:1F", "B8:BE:BF",
									"C0:62:6B", "C4:0A:CB", "C4:64:13", "C4:71:FE", "C4:7D:4F",
									"C8:4C:75", "C8:9C:1D", "C8:F9:F9", "CC:EF:48", "D0:57:4C",
									"D0:C2:82", "D0:D0:FD", "D4:8C:B5", "D4:A0:2A", "D4:D7:48",
									"D8:24:BD", "D8:67:D9", "DC:7B:94", "E0:5F:B9", "E8:04:62",
									"E8:40:40", "E8:B7:48", "E8:BA:70", "EC:30:91", "EC:44:76",
									"EC:C8:82", "F0:25:72", "F0:F7:55", "F4:7F:35", "F4:AC:C1",
									"F4:EA:67", "F8:66:F2", "FC:FB:FB" };

	static final String[] Infineon = { "00:03:19" };

	static final String[] Alps_Electric = {
											"00:02:C7", "00:06:F5", "00:06:F7", "00:07:04",
											"00:16:FE", "00:19:C1", "00:1B:FB", "00:1E:3D",
											"00:21:4F", "00:23:06", "00:24:33", "00:26:43",
											"00:A0:79", "04:76:6E", "04:98:F3", "34:C7:31",
											"38:C0:96", "60:38:0E", "E0:AE:5E" };

	static final String[] nFore = { "00:17:53" };

	static final String[] LinTech = { "00:17:91" };

	static final String[] Sena_Technologies = { "00:01:95" };

	static final String[] Initium = { "00:0B:53" };

	static final String[] Kingjon = { "00:13:EF" };

	static final String[] Gigaset = {
										"00:21:04", "7C:2F:80" };

	static final String[] Metro_Technologies = { "00:0C:A7" };

	static final String[] BlueExpert = { "00:11:B1" };

	static final String[] Mitac = {
									"00:03:53", "00:22:20", "00:22:4D", "00:40:D0" };

	static final String[] Wistron = {
										"00:0A:E4", "00:0B:6B", "00:16:D3", "00:1B:B1", "00:1D:72",
										"00:1F:16", "00:26:2D", "20:6A:8A", "30:14:4A", "3C:97:0E",
										"5C:FF:35", "60:02:B4", "90:A4:DE", "A8:54:B2", "F0:DE:F1",
										"F8:0F:41" };

	static final String[] Baron = { "C8:29:2A" };

	static final String[] Sunitec = { "00:1D:DF" };

	static final String[] Murata = {
									"00:0E:6D", "00:13:E0", "00:21:E8", "00:26:E8", "00:37:6D",
									"00:50:81", "00:60:57", "04:46:65", "14:7D:C5", "20:02:AF",
									"44:A7:CF", "5C:DA:D4", "60:21:C0", "88:30:8A" };

	static final String[] Transystem = { "00:1C:88" };

	static final String[] Novero = { "00:23:3D" };
	static final String[] Parrot = {
									"00:12:1C", "00:26:7E", "90:03:B7" };
	static final String[] Ingenico = {
										"00:03:81", "54:7F:54" };
	static final String[] Bury = { "00:16:73" };

	public static final int Unknown_exp = 2;
	static final int Apple_exp = 5;
	static final int Nokia_exp = 10;
	static final int Samsung_exp = 5;
	static final int LG_exp = 10;
	static final int HTC_exp = 10;
	static final int Motorola_exp = 10;
	static final int Sony_Mobile_Device_exp = 15;
	static final int Sony_Computer_exp = 10;
	static final int Sony_Ericsson_exp = 5;
	static final int BlackBerry_exp = 15;
	static final int Acer_exp = 15;
	static final int Huawei_exp = 20;
	static final int ZTE_exp = 20;
	static final int Panasonic_exp = 20;
	static final int Asus_exp = 20;
	static final int Google_exp = 10;
	static final int Toshiba_exp = 20;
	static final int HP_exp = 15;
	static final int Gigabyte_exp = 25;
	static final int TomTom_exp = 15;
	static final int Siemens_exp = 20;
	static final int Qcom_Bluetooth_Module_exp = 20;
	static final int UGSI_Bluetooth_Module_exp = 10;
	static final int LiteOn_Bluetooth_Module_exp = 15;
	static final int Texas_Instruments_exp = 20;
	static final int Foxconn_exp = 15;
	static final int Temic_SDS_exp = 20;
	static final int Atech_Technology_exp = 20;
	static final int Garmin_exp = 15;
	static final int Cisco_exp = 2;
	static final int Infineon_exp = 20;
	static final int Alps_Electric_exp = 15;
	static final int nFore_exp = 20;
	static final int LinTech_exp = 20;
	static final int Sena_Technologies_exp = 20;
	static final int Initium_exp = 20;
	static final int Kingjon_exp = 15;
	static final int Gigaset_exp = 20;
	static final int Metro_Technologies_exp = 20;
	static final int BlueExpert_exp = 20;
	static final int Mitac_exp = 15;
	static final int Wistron_exp = 15;
	static final int Baron_exp = 20;
	static final int Sunitec_exp = 20;
	static final int Murata_exp = 15;
	static final int Transystem_exp = 20;
	static final int Novero_exp = 20;
	static final int Parrot_exp = 20;
	static final int Ingenico_exp = 20;
	static final int Bury_exp = 20;

}
