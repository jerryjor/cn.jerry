package cn.jerry.lang;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Number64Util {
	private static final BigInteger ENCODE_BIN = new BigInteger("64");
	private static final char ENCODE_ZERO = encodeNumber(BigInteger.ZERO).charAt(0);

	/**
	 * 64以内的数字，用0-9、>、<、A-Z、a-z表示
	 *
	 * @param num
	 * @return
	 */
	public static String encodeNumber(BigInteger num) {
		// 判定负数标识
		String flag = "";
		if (num.compareTo(BigInteger.ZERO) < 0) {
			num = num.negate();
			flag = "-";
		}
		// 把数字用64循环除，余数压缩成字母
		List<Integer> rates = new ArrayList<>();
		BigInteger[] temp;
		do {
			temp = num.divideAndRemainder(ENCODE_BIN);
			rates.add(temp[1].intValue());
			num = temp[0];
		} while (num.compareTo(BigInteger.ZERO) > 0);
		int size = rates.size();
		StringBuilder builder = new StringBuilder(size + 1);
		builder.append(flag);
		char c;
		int rate;
		for (int i = size - 1; i >= 0; i--) {
			rate = rates.get(i);
			if (rate < 10) {
				c = (char) (48 + rate);
			} else if (rate == 10) {
				c = '<';
			} else if (rate == 11) {
				c = '>';
			} else if (rate < 38) {
				c = (char) (53 + rate);
			} else {
				c = (char) (59 + rate);
			}
			builder.append(c);
		}
		return builder.toString();
	}

	/**
	 * 转换0-9、a-z、A-Z到60以内的数字
	 *
	 * @param str
	 * @return
	 */
	public static BigInteger decodeNumber(String str) {
		// 去除高位0
		while (str.charAt(0) == ENCODE_ZERO) {
			str = str.substring(1);
		}
		// 判定负数标识
		boolean negate = false;
		BigInteger result = BigInteger.ZERO;
		if (str.charAt(0) == '-') {
			negate = true;
			str = str.substring(1);
		}
		// 还原10进制数字
		char[] chars = str.toCharArray();
		int num;
		for (int i = chars.length - 1; i >= 0; i--) {
			if (chars[i] <= '9') {
				num = ((int) chars[i]) - 48;
			} else if (chars[i] == '<') {
				num = 10;
			} else if (chars[i] == '>') {
				num = 11;
			} else if (chars[i] <= 'Z') {
				num = ((int) chars[i]) - 53;
			} else {
				num = ((int) chars[i]) - 59;
			}
			result = result.add(new BigInteger("" + num).multiply(ENCODE_BIN.pow(chars.length - i - 1)));
		}
		return negate ? result.negate() : result;
	}

}