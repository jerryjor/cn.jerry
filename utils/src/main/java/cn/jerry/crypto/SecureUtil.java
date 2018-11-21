package cn.jerry.crypto;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.jerry.json.JsonUtil;
import cn.jerry.logging.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 安全工具类
 */
public class SecureUtil {
	private static Logger logger = LogManager.getLogger();

	private static final String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
	        "a", "b", "c", "d", "e", "f" };

	public static String sign(	Object data, String key) {
		StringBuffer param = new StringBuffer();
		List<String> dataKeys = new ArrayList<String>();
		Map<String, Object> dataMap = null;
		try {
			dataMap = JsonUtil.toHashMap(JsonUtil.toJson(data), String.class, Object.class);
		} catch (IOException e) {
			logger.error("read data failed.", e);
			throw new RuntimeException("sign data failed.");
		}
		dataKeys.addAll(dataMap.keySet());
		Collections.sort(dataKeys);
		for (String dataKey : dataKeys) {
			String dataVal = null;
			try {
				dataVal = JsonUtil.toJson(dataMap.get(dataKey));
			} catch (IOException e) {
				logger.error("read property[" + dataKey + "] failed.", e);
			}
			if (dataVal != null && !dataVal.isEmpty()) {
				param.append(dataKey).append("=").append(dataVal).append("&");
			}
		}
		param.append("key=" + key);
		String signStr = signStr(param.toString());
		return signStr;
	}

	public static boolean verify(Object data, String key, String sign) {
		String calcSign = sign(data, key);
		return calcSign.equals(sign);
	}

	private static String signStr(String needSignStr) {
		logger.info("====sign>>>>>> [" + needSignStr + "]");
		String resultString = null;
		try {
			resultString = needSignStr;
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = byteArrayToHexString(md.digest(resultString.getBytes("UTF-8")));
		} catch (Exception e) {
			logger.error("signStr Exception:", e);
		}
		return resultString;
	}

	/**
	 * 转换字节数组为16进制字串
	 * 
	 * @param b
	 *            字节数组
	 * @return 16进制字串
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuilder resultSb = new StringBuilder();
		for (byte aB : b) {
			resultSb.append(byteToHexString(aB));
		}
		return resultSb.toString();
	}

	/**
	 * 转换byte到16进制
	 * 
	 * @param b
	 *            要转换的byte
	 * @return 16进制格式
	 */
	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0) {
			n = 256 + n;
		}
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}

}
