package cn.jerry.lang;

import org.apache.commons.lang.math.RandomUtils;

public class RandomUtil extends RandomUtils {

    /**
     * 获取指定长度随机数字
     * 
     * @param length
     * @return
     */
    public static String getRandomNum(int length) {
        if (length <= 0) return "";
        StringBuilder temp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            temp.append(nextInt(10));
        }
        return temp.toString();
    }

}
