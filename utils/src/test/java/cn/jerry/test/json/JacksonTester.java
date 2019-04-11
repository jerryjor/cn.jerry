package cn.jerry.test.json;

import cn.jerry.json.JsonUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class JacksonTester {
	public static void main(String[] args) {
		int[] baseIp = new int[] { 100, 100, 100, 100 };
		Set<String> blackIps = new HashSet<>();
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

	}
}
