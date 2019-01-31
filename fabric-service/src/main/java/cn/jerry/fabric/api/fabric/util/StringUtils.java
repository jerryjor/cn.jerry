package cn.jerry.fabric.api.fabric.util;

import java.nio.charset.Charset;

public class StringUtils {
	private static final String UTF8_ENCODING = "UTF-8";
	private static final Charset UTF8_CHARSET = Charset.forName(UTF8_ENCODING);

	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	public static boolean isNotEmpty(String str) {
		return str != null && !str.isEmpty();
	}

	public static boolean isBlank(String str) {
		return str == null || str.isEmpty() || str.trim().isEmpty();
	}

	public static boolean isNotBlank(String str) {
		return str != null && !str.isEmpty() && !str.trim().isEmpty();
	}

	public static String trimToNull(String str) {
		return isBlank(str) ? null : str.trim();
	}

	public static String trimToEmpty(String str) {
		return isBlank(str) ? "" : str.trim();
	}

	public static String toString(Object obj, String defaultVal) {
		return obj == null ? defaultVal : obj.toString();
	}

	public static String toString(byte[] bytes) {
		return bytes == null || bytes.length == 0 ? null : new String(bytes, UTF8_CHARSET);
	}

	public static byte[] toBytes(String str) {
		return isEmpty(str) ? null : str.getBytes(UTF8_CHARSET);
	}

	public static String leftPad(String str, char pad, int maxLength) {
		if (str == null) str = "";
		int length = str.length();
		if (length >= maxLength) return str;
		length = maxLength - length;
		StringBuilder builder = new StringBuilder(maxLength);
		for (int i = 0; i < length; i++) {
			builder.append(pad);
		}
		builder.append(str);
		return builder.toString();
	}

	public static String rightPad(String str, char pad, int maxLength) {
		if (str == null) str = "";
		int length = str.length();
		if (length >= maxLength) return str;
		length = maxLength - length;
		StringBuilder builder = new StringBuilder(maxLength);
		builder.append(str);
		for (int i = 0; i < length; i++) {
			builder.append(pad);
		}
		return builder.toString();
	}

}
