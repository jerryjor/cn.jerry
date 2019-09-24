package cn.jerry.lang;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个把纯数字转换成字符，从而压缩字符串长度的工具类
 */
public class Number64Util {
    private static final BigInteger ENCODE_BIN = new BigInteger("64");
    private static final char ENCODE_ZERO = encodeNumber(BigInteger.ZERO).charAt(0);

    /**
     * 64以内的数字，用+、/、0-9、A-Z、a-z表示
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
        List<Integer> rates = new ArrayList<Integer>();
        BigInteger[] temp;
        do {
            temp = num.divideAndRemainder(ENCODE_BIN);
            rates.add(temp[1].intValue());
            num = temp[0];
        } while (num.compareTo(BigInteger.ZERO) > 0);
        int size = rates.size();
        StringBuilder builder = new StringBuilder(size + 1);
        builder.append(flag);
        for (int i = size - 1; i >= 0; i--) {
            builder.append(toChar(rates.get(i)));
        }
        return builder.toString();
    }

    private static char toChar(int i) {
        if (i == 0) {
            return '+';
        } else if (i == 1) {
            return '/';
        } else if (i < 12) {
            return (char) (46 + i);
        } else if (i < 38) {
            return (char) (53 + i);
        } else {
            return (char) (59 + i);
        }
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
        for (int i = chars.length - 1; i >= 0; i--) {
            result = result.add(new BigInteger(Integer.toString(toInt(chars[i]))).multiply(ENCODE_BIN.pow(chars.length - i - 1)));
        }
        return negate ? result.negate() : result;
    }

    private static int toInt(char c) {
        if (c == '+') {
            return 0;
        } else if (c == '/') {
            return 1;
        } else if (c <= '9') {
            return ((int) c) - 46;
        } else if (c <= 'Z') {
            return ((int) c) - 53;
        } else {
            return ((int) c) - 59;
        }
    }
}