/**
 *  MacAdressAllocations.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



import java.lang.reflect.Field;
import java.util.HashMap;



/**
 * 
 * Addresses from <i>IEEE STANDARDS ASSOCIATION</i>.
 * 
 * @author Maksl5[Markus Bensing]
 * 
 */
public class MacAdressAllocations {

	private static HashMap<String, String[ ]> manufacturerMacs = null;
	private static HashMap<String, Integer> manufacturerExps = null;

	static {
		HashMap<String, String[ ]> hsHashMap = new HashMap<String, String[ ]>();

		Field[ ] fields = MacAdressAllocations.class.getDeclaredFields();

		for (Field field : fields) {
			if (field.getType().equals(String[ ].class)) {
				String[ ] macStrings;
				try {
					macStrings = (String[ ]) field.get(MacAdressAllocations.class);
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
	}

	public static HashMap<String, String[ ]> getHashMap() {

		if (manufacturerMacs != null) {
			if (!manufacturerMacs.isEmpty()) { return manufacturerMacs; }
		}

		HashMap<String, String[ ]> hsHashMap = new HashMap<String, String[ ]>();

		Field[ ] fields = MacAdressAllocations.class.getDeclaredFields();

		for (Field field : fields) {
			if (field.getType().equals(String[ ].class)) {
				String[ ] macStrings;
				try {
					macStrings = (String[ ]) field.get(MacAdressAllocations.class);
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

	public static int getExp(String manufacturer) {

		if (manufacturerExps != null) {
			if (!manufacturerExps.isEmpty()) {
				if (manufacturerExps.get(manufacturer
											+ "_exp") != null) { return manufacturerExps.get(manufacturer
																								+ "_exp"); }
			}
		}

		int exp = 0;

		try {
			Field expField = MacAdressAllocations.class.getDeclaredField(manufacturer
																			+ "_exp");
			exp = expField.getInt(MacAdressAllocations.class);
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

		Field[ ] fields = MacAdressAllocations.class.getDeclaredFields();

		for (Field field : fields) {
			if (field.getType().equals(int.class)) {
				int exp;
				try {
					exp = (Integer) field.get(MacAdressAllocations.class);
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

	static final String[ ] Apple =
			{
					"00:03:93", "00:05:02", "00:0A:27", "00:0A:95", "00:0D:93", "00:10:FA", "00:11:24", "00:14:51",
					"00:16:CB", "00:17:F2", "00:19:E3", "00:1B:63", "00:1C:B3", "00:1D:4F", "00:1E:52", "00:1E:C2",
					"00:1F:5B", "00:1F:F3", "00:21:E9", "00:22:41", "00:23:12", "00:23:32", "00:23:6C", "00:23:DF",
					"00:24:36", "00:25:00", "00:25:4B", "00:25:BC", "00:26:08", "00:26:4A", "00:26:B0", "00:26:BB",
					"00:30:65", "00:3E:E1", "00:50:E4", "00:A0:3F", "00:A0:40", "00:C6:10", "00:F4:B9", "04:0C:CE",
					"04:1E:64", "04:54:53", "08:00:07", "0C:74:C2", "0C:77:1A", "10:40:F3", "10:93:E9", "10:9A:DD",
					"10:DD:B1", "14:10:9F", "14:5A:05", "14:8F:C6", "18:20:32", "18:34:51", "18:9E:FC", "18:E7:F4",
					"1C:AB:A7", "20:C9:D0", "24:AB:81", "28:37:37", "28:6A:B8", "28:6A:BA", "28:CF:DA", "28:E0:2C",
					"28:E7:CF", "34:15:9E", "34:51:C9", "3C:07:54", "3C:D0:F8", "40:30:04", "40:3C:FC", "40:6C:8F",
					"40:A6:D9", "40:D3:2D", "44:2A:60", "44:4C:0C", "44:D8:84", "48:60:BC", "4C:B1:99", "50:EA:D6",
					"54:26:96", "58:1F:AA", "58:55:CA", "58:B0:35", "5C:59:48", "5C:95:AE", "60:33:4B", "60:C5:47",
					"60:FA:CD", "60:FB:42", "64:20:0C", "64:B9:E8", "64:E6:82", "68:09:27", "68:96:7B", "68:A8:6D",
					"6C:3E:6D", "6C:C2:6B", "70:56:81", "70:73:CB", "70:CD:60", "70:DE:E2", "74:E1:B6", "74:E2:F5",
					"78:A3:E4", "78:CA:39", "7C:11:BE", "7C:6D:62", "7C:C3:A1", "7C:C5:37", "7C:D1:C3", "7C:F0:5F",
					"80:49:71", "80:92:9F", "84:29:99", "84:85:06", "84:FC:FE", "88:53:95", "88:C6:63", "8C:2D:AA",
					"8C:58:77", "8C:7B:9D", "8C:FA:BA", "90:27:E4", "90:84:0D", "98:03:D8", "98:B8:E3", "98:D6:BB",
					"98:FE:94", "9C:20:7B", "A4:67:06", "A4:B1:97", "A4:D1:D2", "A8:20:66", "B0:65:BD", "B4:F0:AB",
					"B8:17:C2", "B8:8D:12", "B8:C7:5D", "B8:F6:B1", "B8:FF:61", "BC:52:B7", "BC:67:78", "C0:84:7A",
					"C0:9F:42", "C4:2C:03", "C8:2A:14", "C8:33:4B", "C8:BC:C8", "CC:08:E0", "D0:23:DB", "D4:9A:20",
					"D8:00:4D", "D8:30:62", "D8:9E:3F", "D8:A2:5E", "D8:D1:CB", "DC:2B:61", "E0:B9:BA", "E0:C9:7A",
					"E0:F8:47", "E4:8B:7F", "E4:CE:8F", "E8:04:0B", "E8:06:88", "EC:85:2F", "F0:B4:79", "F0:CB:A1",
					"F0:DC:E2", "F8:1E:DF", "FC:25:3F" };

	static final String[ ] Nokia = {
			"00:02:EE", "00:0B:E1", "00:0E:ED", "00:0F:BB", "00:10:B3", "00:11:9F", "00:12:62", "00:13:70", "00:13:FD",
			"00:14:A7", "00:15:2A", "00:15:A0", "00:15:DE", "00:16:4E", "00:16:BC", "00:17:4B", "00:17:B0", "00:18:0F",
			"00:18:42", "00:18:8D", "00:18:C5", "00:19:2D", "00:19:4F", "00:19:79", "00:19:B7", "00:1A:16", "00:1A:89",
			"00:1A:DC", "00:1B:33", "00:1B:AF", "00:1B:EE", "00:1C:35", "00:1C:9A", "00:1C:D4", "00:1C:D6", "00:1D:3B",
			"00:1D:6E", "00:1D:98", "00:1D:E9", "00:1D:FD", "00:1E:3A", "00:1E:3B", "00:1E:A3", "00:1E:A4", "00:1F:00",
			"00:1F:01", "00:1F:5C", "00:1F:5D", "00:1F:DE", "00:1F:DF", "00:21:08", "00:21:09", "00:21:AA", "00:21:AB",
			"00:21:FC", "00:21:FE", "00:22:65", "00:22:66", "00:22:FC", "00:22:FD", "00:23:B4", "00:24:03", "00:24:04",
			"00:24:7C", "00:24:7D", "00:25:47", "00:25:48", "00:25:CF", "00:25:D0", "00:26:68", "00:26:69", "00:26:CC",
			"00:40:43", "00:BD:3A", "00:E0:03", "04:5A:95", "04:A8:2A", "0C:C6:6A", "0C:DD:EF", "10:F9:EE", "14:36:05",
			"18:14:56", "18:86:AC", "20:D6:07", "28:D1:AF", "2C:D2:E7", "30:38:55", "34:7E:39", "34:C8:03", "38:19:2F",
			"3C:36:3D", "3C:F7:2A", "48:DC:FB", "50:2D:1D", "5C:57:C8", "6C:9B:02", "6C:A7:80", "6C:E9:07", "78:2E:EF",
			"78:CA:04", "80:50:1B", "90:CF:15", "94:00:70", "94:20:53", "94:3A:F0", "9C:18:74", "9C:4A:7B", "9C:CA:D9",
			"A0:4E:04", "A0:71:A9", "A0:F4:19", "A4:E7:31", "A8:7B:39", "A8:7E:33", "A8:E0:18", "AC:81:F3", "AC:93:2F",
			"B0:35:8D", "B0:5C:E5", "C0:38:F9", "C8:3D:97", "C8:97:9F", "C8:DF:7C", "D0:DB:32", "D4:5D:42", "D4:C1:FC",
			"D4:CB:AF", "D8:2A:7E", "D8:75:33", "DC:9F:A4", "E0:A6:70", "E4:EC:10", "E8:CB:A1", "EC:9B:5B", "F4:8E:09",
			"F8:5F:2A", "FC:E5:57" };

	static final String[ ] Samsung = {
			"00:00:F0", "00:02:78", "00:07:AB", "00:12:47", "00:12:FB", "00:13:77", "00:15:99", "00:15:B9", "00:16:32",
			"00:16:6B", "00:16:6C", "00:16:DB", "00:17:C9", "00:17:D5", "00:18:AF", "00:1A:8A", "00:1B:98", "00:1C:43",
			"00:1D:25", "00:1D:F6", "00:1E:7D", "00:1E:E1", "00:1E:E2", "00:1F:CC", "00:1F:CD", "00:21:19", "00:21:4C",
			"00:21:D1", "00:21:D2", "00:23:39", "00:23:3A", "00:23:99", "00:23:C2", "00:23:D6", "00:23:D7", "00:24:54",
			"00:24:90", "00:24:91", "00:24:E9", "00:25:38", "00:25:66", "00:25:67", "00:26:37", "00:26:5D", "00:26:5F",
			"00:E0:64", "04:18:0F", "04:FE:31", "08:D4:2B", "0C:71:5D", "0C:DF:A4", "10:1D:C0", "18:3F:47", "18:46:17",
			"18:E2:C2", "1C:62:B8", "1C:66:AA", "20:13:E0", "20:64:32", "28:98:7B", "2C:44:01", "34:C3:AC", "38:0A:94",
			"38:16:D1", "38:AA:3C", "38:EC:E4", "3C:5A:37", "3C:62:00", "3C:8B:FE", "44:4E:1A", "44:F4:59", "48:44:F7",
			"4C:BC:A5", "50:01:BB", "50:B7:C3", "50:CC:F8", "54:92:BE", "54:9B:12", "58:C3:8B", "5C:0A:5B", "5C:E8:EB",
			"60:6B:BD", "60:A1:0A", "60:D0:A9", "68:EB:AE", "6C:83:36", "70:F9:27", "74:45:8A", "78:25:AD", "78:47:1D",
			"78:59:5E", "78:D6:F0", "84:0B:2D", "84:25:DB", "8C:71:F8", "8C:77:12", "8C:C8:CD", "90:18:7C", "94:51:03",
			"94:63:D1", "98:0C:82", "98:52:B1", "9C:02:98", "A0:07:98", "A0:0B:BA", "A0:21:95", "A0:75:91", "A8:F2:74",
			"B0:D0:9C", "B0:EC:71", "B4:07:F9", "B4:62:93", "B8:C6:8E", "B8:D9:CE", "BC:20:A4", "BC:47:60", "BC:85:1F",
			"BC:B1:F3", "C8:19:F7", "C8:7E:75", "CC:05:1B", "CC:F9:E8", "CC:FE:3C", "D0:17:6A", "D0:66:7B", "D0:C1:B1",
			"D0:DF:C7", "D4:87:D8", "D4:88:90", "D4:E8:B2", "D8:57:EF", "DC:71:44", "E4:7C:F9", "E4:B0:21", "E4:E0:C5",
			"E8:03:9A", "E8:11:32", "E8:E5:D6", "EC:E0:9B", "F0:08:F1", "F0:E7:7E", "F4:9F:54", "F4:D9:FB", "F8:D0:BD",
			"FC:A1:3E", "FC:C7:34" };

	static final String[ ] LG = {
			"00:1C:62", "00:1E:75", "00:1F:6B", "00:1F:E3", "00:21:FB", "00:22:A9", "00:24:83", "00:25:E5", "00:26:E2",
			"00:AA:70", "00:E0:91", "10:F9:6F", "20:21:A5", "3C:BD:D8", "6C:D6:8A", "70:05:14", "74:A7:22", "A8:16:B2",
			"A8:92:2C", "C0:41:F6", "E8:5B:5B", "E8:92:A4", "F0:1C:13", "F8:0C:F3" };

	static final String[ ] HTC = {
			"00:09:2D", "00:23:76", "18:87:96", "1C:B0:94", "38:E7:D8", "64:A7:69", "7C:61:93", "90:21:55", "A0:F4:50",
			"A8:26:D9", "BC:CF:CC", "D4:20:6D", "D8:B3:77", "E8:99:C4", "F8:DB:7F" };

	static final String[ ] Motorola = {
			"00:04:BD", "00:08:0E", "00:0B:06", "00:0C:E5", "00:0E:5C", "00:0F:9F", "00:11:1A", "00:11:80", "00:11:AE",
			"00:12:25", "00:12:8A", "00:12:C9", "00:13:71", "00:14:04", "00:14:9A", "00:14:E8", "00:15:2F", "00:15:9A",
			"00:15:A8", "00:16:26", "00:16:75", "00:16:B4", "00:16:B5", "00:17:00", "00:17:84", "00:17:E2", "00:17:EE",
			"00:18:A4", "00:18:C0", "00:19:2C", "00:19:5E", "00:19:A6", "00:19:C0", "00:1A:1B", "00:1A:66", "00:1A:77",
			"00:1A:AD", "00:1A:DB", "00:1A:DE", "00:1B:52", "00:1B:DD", "00:1C:11", "00:1C:12", "00:1C:C1", "00:1C:FB",
			"00:1D:BE", "00:1E:46", "00:1E:5A", "00:1E:8D", "00:1F:7E", "00:1F:C4", "00:21:1E", "00:21:36", "00:21:43",
			"00:21:80", "00:22:10", "00:22:B4", "00:23:0B", "00:23:74", "00:23:75", "00:23:95", "00:23:A2", "00:23:A3",
			"00:23:AF", "00:23:EE", "00:24:95", "00:24:A0", "00:24:A1", "00:24:C1", "00:25:F1", "00:25:F2", "00:26:36",
			"00:26:BA", "14:5B:D1", "1C:14:48", "20:E5:64", "2C:9E:5F", "38:6B:BB", "3C:43:8E", "3C:75:4A", "40:B7:F3",
			"40:FC:89", "64:ED:57", "6C:C1:D2", "70:7E:43", "74:56:12", "74:E7:C6", "74:F6:12", "7C:BF:B1", "80:96:B1",
			"90:B1:34", "94:CC:B9", "98:4B:4A", "A4:7A:A4", "A4:ED:4E", "B0:77:AC", "B0:79:94", "C8:AA:21", "CC:7D:37",
			"DC:45:17", "E4:64:49", "E4:83:99", "E8:6D:52", "F8:7B:7A" };

	static final String[ ] Sony_Mobile_Device = {
			"20:54:76", "D0:51:62" };

	static final String[ ] Sony_Computer = {
			"00:01:4A", "00:04:1F", "00:13:15", "00:13:A9", "00:15:C1", "00:19:C5", "00:1A:80", "00:1D:0D", "00:1D:BA",
			"00:1F:A7", "00:22:A6", "00:24:8D", "00:24:BE", "08:00:46", "28:0D:FC", "30:F9:ED", "3C:07:71", "54:42:49",
			"54:53:ED", "78:84:3C", "A8:E3:EE", "F0:BF:97", "F8:D0:AC", "FC:0F:E6" };

	static final String[ ] Sony_Ericsson = {
			"00:0A:D9", "00:0E:07", "00:0F:DE", "00:12:EE", "00:16:20", "00:16:B8", "00:18:13", "00:19:63", "00:1A:75",
			"00:1B:59", "00:1C:A4", "00:1D:28", "00:1E:45", "00:1E:DC", "00:1F:E4", "00:21:9E", "00:22:98", "00:23:45",
			"00:23:F1", "00:24:EF", "00:25:E7", "24:21:AB", "30:17:C8", "30:39:26", "40:2B:A1", "58:17:0C", "5C:B5:24",
			"6C:0E:0D", "6C:23:B9", "84:00:D2", "8C:64:22", "90:C1:15", "B8:F9:34" };

	static final String[ ] BlackBerry = {
			"00:0F:86", "00:1C:CC", "00:25:57", "00:26:FF", "1C:69:A5", "34:BB:1F", "40:6F:2A", "68:ED:43", "70:AA:B2",
			"F4:0B:93", "00:23:7A", "14:74:11", "2C:A8:35", "30:69:4B", "30:7C:30", "3C:74:37", "40:5F:BE", "40:6A:AB",
			"70:D4:F2", "80:60:07", "A0:6C:EC", "A8:6A:6F", "CC:55:AD", "E8:3E:B6" };

	static final String[ ] Acer = {
			"00:01:24", "00:00:E2", "00:A0:60" };

	static final String[ ] Huawei = {
			"00:18:82", "00:25:9D", "00:25:9E", "00:46:4B", "00:E0:FC", "08:19:A6", "0C:37:DC", "10:1B:54", "10:47:80",
			"10:C6:1F", "20:F3:A3", "28:3C:E4", "28:6E:D4", "34:6B:D3", "4C:1F:CC", "4C:B1:6C", "54:89:98", "60:DE:44",
			"70:72:3C", "70:7B:E8", "78:1D:BA", "78:F5:FD", "80:B6:86", "80:FB:06", "88:53:D4", "A4:99:47", "AC:DE:48",
			"AC:E2:15", "AC:E8:7B", "C8:D1:5E", "CC:CC:81", "DC:D2:FC", "E0:24:7F", "EC:23:3D", "F4:55:9C", "F8:3D:FF",
			"FC:48:EF" };

	static final String[ ] ZTE = {
			"00:15:EB", "00:19:C6", "00:1E:73", "00:22:93", "00:25:12", "00:26:ED", "08:18:1A", "34:4B:50", "34:E0:CF",
			"38:46:08", "48:28:2F", "4C:09:B4", "4C:AC:0A", "68:1A:B2", "84:74:2A", "8C:E0:81", "98:F5:37", "9C:D2:4B",
			"B0:75:D5", "B4:B3:62", "C8:64:C7", "C8:7B:5B", "D0:15:4A", "DC:02:8E", "F4:6D:E2", "FC:C8:97" };

	static final String[ ] Panasonic = {
			"00:19:87", "D8:B1:2A" };

	static final String[ ] Asus = {
			"00:0C:6E", "00:0E:A6", "00:11:2F", "00:11:D8", "00:13:D4", "00:15:F2", "00:17:31", "00:18:F3", "00:1A:92",
			"00:1B:FC", "00:1D:60", "00:1E:8C", "00:1F:C6", "00:22:15", "00:23:54", "00:24:8C", "00:26:18", "00:E0:18",
			"10:BF:48", "14:DA:E9", "20:CF:30", "30:85:A9", "48:5B:39", "50:46:5D", "54:04:A6", "90:E6:BA", "BC:AE:C5",
			"C8:60:00", "E0:CB:1D", "E0:CB:4E", "F4:6D:04" };

	static final String[ ] Google = {
			"00:1A:11", "F8:8F:CA" };

	static final String[ ] Toshiba = {
		"00:00:39" };

	static final String[ ] HP = {
			"00:01:E6", "00:01:E7", "00:02:A5", "00:04:EA", "00:08:02", "00:08:83", "00:08:C7", "00:09:FB", "00:0A:57",
			"00:0B:CD", "00:0D:9D", "00:0E:7F", "00:0E:B3", "00:0F:20", "00:0F:61", "00:10:83", "00:10:E3", "00:11:0A",
			"00:11:85", "00:12:79", "00:13:21", "00:14:38", "00:14:C2", "00:15:60", "00:16:35", "00:17:08", "00:17:A4",
			"00:18:71", "00:18:FE", "00:19:BB", "00:1A:4B", "00:1B:78", "00:1C:C4", "00:1E:0B", "00:1F:29", "00:21:5A",
			"00:22:64", "00:23:7D", "00:24:81", "00:25:B3", "00:26:55", "00:30:C1", "00:50:8B", "00:60:B0", "00:80:5F",
			"00:9C:02", "10:1F:74", "18:A9:05", "1C:C1:DE", "2C:27:D7", "2C:41:38", "2C:76:8A", "3C:4A:92", "3C:D9:2B",
			"44:1E:A1", "64:31:50", "68:B5:99", "78:AC:C0", "78:E3:B5", "78:E7:D1", "98:4B:E1", "9C:8E:99", "B4:99:BA",
			"B8:AF:67", "D4:85:64", "D8:D3:85", "F4:CE:46" };

	static final String[ ] Gigabyte = {
			"00:0D:61", "00:0F:EA", "00:16:E6", "00:1A:4D", "00:1D:7D", "00:1F:D0", "00:20:ED", "00:24:1D", "1C:6F:65",
			"50:E5:49", "6C:F0:49", "90:2B:34" };

	static final String[ ] TomTom = {
			"00:13:6C", "00:21:3E" };

	static final String[ ] Siemens = {
			"00:01:E3", "00:0B:A3", "00:0D:41", "00:0E:8C", "00:19:28", "00:1B:1B", "00:1F:F8", "00:23:41", "08:00:06",
			"40:EC:F8", "78:9F:87", "88:4B:39" };

	static final String[ ] Qcom_Bluetooth_Module = {
		"00:0D:F0" };
	static final String[ ] UGSI_Bluetooth_Module = {
			"00:10:C6", "00:16:41", "00:1A:6B", "00:1E:37", "00:21:86", "00:24:7E", "00:27:13", "40:2C:F4", "70:F3:95",
			"CC:52:AF", "E0:2A:82", "FC:4D:D4" };
	static final String[ ] LiteOn_Bluetooth_Module = {
			"00:22:5F", "1C:65:9D", "20:68:9D", "44:6D:57", "68:A3:C4", "70:F1:A1", "74:DE:2B", "74:E5:43", "9C:B7:0D",
			"D0:DF:9A" };

	static final int Unknown_exp = 12;
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

}
