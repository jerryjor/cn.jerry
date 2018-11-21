package cn.jerry.test.json;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import cn.jerry.json.JsonUtil;

public class JacksonTester {
	public static void main(String[] args) {
		int[] baseIp = new int[] { 100, 100, 100, 100 };
		Set<String> blackIps = new HashSet<String>();
		String ip = null;
		for (int i = 0; i < 50000; i++) {
			ip = baseIp[0] + "." + baseIp[1] + "." + baseIp[2] + "." + baseIp[3];
			blackIps.add(ip);
			baseIp[3]++;
			for (int j = 3; j > 0; j--) {
				if (baseIp[j] == 256) {
					baseIp[j] = 100;
					baseIp[j - 1]++;
				}
			}
		}
		System.out.println("last ip:" + ip);
		int loop = 1000;
		long st = System.currentTimeMillis();
		String json;
		for (int i = 0; i < loop; i++) {
			try {
				json = JsonUtil.toJson(blackIps);
				JsonUtil.toHashSet(json, String.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long et = System.currentTimeMillis();
		System.out.println("json read/write " + loop + "times, cost " + (et - st) + "ms.");

		// KeyValue<String, Number> kv = new KeyValue<String, Number>("1", 1);
		// try {
		// String json = JsonUtil.toJson(kv);
		// @SuppressWarnings("unchecked")
		// KeyValue<String, Number> kv1 = JsonUtil.toObject(json, KeyValue.class, String.class,
		// Long.class);
		// System.out.println(kv1);
		// System.out.println(kv1.getKey().getClass().getSimpleName());
		// System.out.println(kv1.getValue().getClass().getSimpleName());
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// List<KeyValue<String, Number>> list = new ArrayList<KeyValue<String, Number>>();
		// for (int i = 1; i < 6; i++) {
		// list.add(new KeyValue<String, Number>(""+i,i));
		// }
		// String json = JsonUtil.toJsonSilently(list);
		// System.out.println(json);
		// try {
		// Set<Map<String, Number>> result2 = JsonUtil.toHashSetFillWithHashMap(json,String.class,
		// Number.class);
		// System.out.println(result2);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// Map<String, List<Number>> map = new HashMap<String, List<Number>>();
		// for (int i = 0; i < 3; i++) {
		// List<Number> list = new ArrayList<Number>();
		// map.put("" + i, list);
		// for (int j = 0; j < 3; j++) {
		// list.add(j);
		// }
		// }
		// try {
		// String json = JsonUtil.toJson(map);
		// Map<String, Set<Number>> result = JsonUtil.toHashMapFillWithHashSet(json, String.class,
		// Number.class);
		// System.out.println(result);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}
