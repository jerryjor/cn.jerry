package cn.jerry.lang;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UqStringTool {
	private static final String ULE_PRE = "U-";
	private static final String LOCAL_IP = readLocalIp();
	private static final long MS_ZONE_OFFSET = Calendar.getInstance().get(Calendar.ZONE_OFFSET);
	private static final long MS_PER_DAY = 24 * 60 * 60 * 1000L;
	private static final long MS_PER_HOUR = 60 * 60 * 1000L;
	private static final long MS_PER_MIN = 60 * 1000L;
	private static final long MS_PER_SEC = 1000L;
	private static final int ENCODE_BIN = 64;
	private static int seq = 0;
	private static Random r = new Random();

	/**
	 * 基于IP+时间生成当天伪唯一的流水号
	 * 并发过高,位数过少时不保证唯一性
	 * 
	 * @param useUlePre 是否以"U-"开头
	 * @param length 长度
	 * @param occursPerSec 每秒最大并发数
	 * @return
	 */
	public static String genUqNo(boolean useUlePre, int length, int occursPerSec) {
		return useUlePre ? ULE_PRE + genUqNo(length - ULE_PRE.length(), occursPerSec)
		        : genUqNo(length, occursPerSec);
	}

	/**
	 * 基于IP+时间生成当天伪唯一的流水号
	 * 并发过高,位数过小时不保证唯一性
	 * 
	 * @param length 长度
	 * @param occursPerSec 每秒最大并发数
	 * @return
	 */
	public static String genUqNo(int length, int occursPerSec) {
		String ipAndTime = genHexNumByIpAndTime();
		int randomLength = Long.valueOf(Math.round(Math.log10(occursPerSec))).intValue();
		if (length >= randomLength + ipAndTime.length()) {
			return ipAndTime + genRandomHexNum(length - ipAndTime.length());
		} else {
			throw new RuntimeException("length not enough.");
		}
	}

	/**
	 * 取当前IP+时间，转为7位字符
	 * 
	 * @return
	 */
	private static String genHexNumByIpAndTime() {
		StringBuilder temp = new StringBuilder(16);
		// 基于IP最后一段，防止局域网内多服务并行
		temp.append(LOCAL_IP);
		// 基于当前时间
		Long currTime = System.currentTimeMillis() + MS_ZONE_OFFSET;
		currTime = currTime % MS_PER_DAY;
		temp.append(encodeNumer(currTime / MS_PER_HOUR));
		currTime = currTime % MS_PER_HOUR;
		temp.append(encodeNumer(currTime / MS_PER_MIN));
		currTime = currTime % MS_PER_MIN;
		temp.append(encodeNumer(currTime / MS_PER_SEC));
		currTime = currTime % MS_PER_SEC;
		temp.append(encodeNumer(currTime)).append(encodeNumer(currTime / ENCODE_BIN));

		return temp.toString();
	}

	/**
	 * 读取当前机器最后一段ip，转为2位64进制字符
	 * 
	 * @return
	 */
	private static String readLocalIp() {
		InetAddress addr = null;
		long ip;
		try {
			addr = InetAddress.getLocalHost();
			String ipAddr = addr.getHostAddress();
			ipAddr = ipAddr.substring(ipAddr.lastIndexOf(".") + 1);
			ip = Long.valueOf(ipAddr);
		} catch (Exception e) {
			ip = 0L;
		}
		return encodeNumer(ip) + encodeNumer(ip / ENCODE_BIN);
	}

	/**
	 * 生成指定长度随机字符
	 * 
	 * @param length
	 * @return
	 */
	private static String genRandomHexNum(int length) {
		StringBuilder temp = new StringBuilder(16);
		String nextSeq = nextSeq();
		if (length > nextSeq.length()) {
			temp.append(nextSeq);
			length = length - nextSeq.length();
		}
		for (int i = 0; i < length; i++) {
			temp.append(encodeNumer(r.nextInt(62) * 1L));
		}
		return temp.toString();
	}

	/**
	 * 获取00-ff范围内的下一个序列，转成2位64进制字符
	 * 
	 * @return
	 */
	private static String nextSeq() {
		int nextSeq;
		synchronized (LOCAL_IP) {
			nextSeq = ++seq % 1024;
		}
		return encodeNumer(nextSeq) + encodeNumer(nextSeq / ENCODE_BIN);
	}

	/**
	 * 64以内的数字，用0-9、a-z、A-Z、+、-表示
	 * 
	 * @param num
	 * @return
	 */
	private static String encodeNumer(long num) {
		num = num % ENCODE_BIN;
		if (num < 10) {
			return String.valueOf((char) (48 + num));
		} else if (num < 36) {
			return String.valueOf((char) (55 + num));
		} else if (num < 62) {
			return String.valueOf((char) (61 + num));
		} else if (num == 62) {
			return "+";
		} else {
			return "-";
		}
	}

	public static Map<String, String> readUqNo(String uqno) {
		Map<String, String> data = new HashMap<String, String>();
		if (uqno.startsWith(ULE_PRE)) {
			uqno = uqno.substring(ULE_PRE.length());
		}
		if (uqno.length() < 7) return data;

		try {
			// 解析ip
			int ip = ENCODE_BIN * decodeChar(uqno.charAt(1)) + decodeChar(uqno.charAt(0));
			data.put("ip", "" + ip);
			uqno = uqno.substring(2);

			// 解析时间
			String time = "" + decodeChar(uqno.charAt(0)) + ":" + decodeChar(uqno.charAt(1))
			        + ":" + decodeChar(uqno.charAt(2)) + ":"
			        + (ENCODE_BIN * decodeChar(uqno.charAt(4)) + decodeChar(uqno.charAt(3)));
			data.put("time", time);
			uqno = uqno.substring(5);

			// 解析seq
			if (uqno.length() > 2) {
				int seq = ENCODE_BIN * decodeChar(uqno.charAt(1)) + decodeChar(uqno.charAt(0));
				data.put("seq", "" + seq);
				uqno = uqno.substring(2);
			}

			// 解析随机数
			String random = "";
			for (int i = 0; i < uqno.length(); i++) {
				random += decodeChar(uqno.charAt(i));
			}
			data.put("random", random);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}

	/**
	 * 转换0-9、a-z、A-Z到60以内的数字
	 * 
	 * @param c
	 * @return
	 */
	private static int decodeChar(char c) {
		if (c == '+') {
			return 62;
		} else if (c == '-') {
			return 63;
		} else if (c <= '9') {
			return ((int) c) - 48;
		} else if (c <= 'Z') {
			return ((int) c) - 55;
		} else {
			return ((int) c) - 61;
		}
	}

}