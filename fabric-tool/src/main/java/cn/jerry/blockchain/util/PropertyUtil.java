package cn.jerry.blockchain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 读取properties文件
 */
public class PropertyUtil {
    private static final Logger log = LoggerFactory.getLogger(PropertyUtil.class);

	private static final Map<String, Properties> cache = new HashMap<>();

    private PropertyUtil() {
        super();
    }

	public static Properties get(String configFileName) {
		if (cache.get(configFileName) != null) {
			return cache.get(configFileName);
		}

		Properties config = new Properties();
		try (InputStreamReader isr = new InputStreamReader(PropertyUtil.class.getResourceAsStream(configFileName), StandardCharsets.UTF_8)) {
			config.load(isr);
			cache.put(configFileName, config);
		} catch (IOException e) {
			log.error("load property failed, configFileName:" + configFileName, e);
		}
		return config;
	}

	public static String getStringValue(String configFileName, String key) {
		Properties p = get(configFileName);
		String value = null;
		if (p != null) {
			value = (String) p.get(key);
		}
		return value;
	}

	public static List<String> getListValue(String configFileName, String key) {
		List<String> listVal = new ArrayList<>();

		String strVal = getStringValue(configFileName, key);
		if (strVal == null || strVal.isEmpty()) return listVal;

		String[] arrayVal = strVal.split(",");
		for (String str : arrayVal) {
			str = str.trim();
			if (!str.isEmpty()) {
				listVal.add(str);
			}
		}

		return listVal;
	}

	public static String[] getArrayValue(String configFileName, String key) {
		List<String> listVal = getListValue(configFileName, key);
		return listVal.isEmpty() ? null : listVal.toArray(new String[0]);
	}

	public static BigDecimal getBigDecimalValue(String configFileName, String key) {
		String strVal = getStringValue(configFileName, key);
		BigDecimal bdVal = null;
		if (strVal != null && !strVal.isEmpty()) {
			try {
				bdVal = new BigDecimal(strVal);
			} catch (Exception e) {
				log.error("getBigDecimalValue failed, [{}] is not a number!", strVal);
			}
		}
		return bdVal;
	}

	public static Integer getIntValue(String configFileName, String key) {
		BigDecimal bdVal = getBigDecimalValue(configFileName, key);
		return bdVal == null ? null : bdVal.intValue();
	}

	public static Long getLongValue(String configFileName, String key) {
		BigDecimal bdVal = getBigDecimalValue(configFileName, key);
		return bdVal == null ? null : bdVal.longValue();
	}

	public static Double getDoubleValue(String configFileName, String key) {
		BigDecimal bdVal = getBigDecimalValue(configFileName, key);
		return bdVal == null ? null : bdVal.doubleValue();
	}
}
