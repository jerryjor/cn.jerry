package cn.jerry.lang;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class StringUtil extends StringUtils {

    /**
     * 分割字符串
     * 
     * @param str 字符串
     * @param split 分隔符
     * @param jumpBlank 是否跳过空字符串
     * @return
     */
    public static String[] split(String str, String split, boolean jumpBlank) {
        if (str == null) {
            return null;
        }
        List<String> parts = new ArrayList<String>();
        int start = 0, end = -1, size = 0;
        String part;
        while (true) {
            end = str.indexOf(split, start);
            if (end == -1) end = str.length();
            part = str.substring(start, end);
            if (!jumpBlank || !part.isEmpty()) {
                size++;
                parts.add(part);
            }
            start = end + split.length();
            if (end == str.length()) break;
        }
        return parts.toArray(new String[size]);
    }
}
