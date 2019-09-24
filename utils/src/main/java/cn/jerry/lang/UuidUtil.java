package cn.jerry.lang;

import java.util.UUID;

public class UuidUtil {

    public static String random() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
