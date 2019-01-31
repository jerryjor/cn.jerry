package cn.jerry.fabric.api.tools.test;

import cn.jerry.fabric.api.fabric.convert.StringToArrayConverter;
import cn.jerry.fabric.api.fabric.util.JsonUtil;

import java.io.IOException;

public class StringToArrayConverterTester {
    public static void main(String[] args) {
        String str = null;//"[\"a\",\"b\",\"c\"]";
        try {
            str = JsonUtil.toJsonNonNull(new String[]{"a","b","c"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringToArrayConverter converter = new StringToArrayConverter();
        for (String s : converter.convert(str)) {
            System.out.println(s);
        }
    }
}
