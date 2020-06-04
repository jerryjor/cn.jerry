package cn.jerry.fabric.api.fabric.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Util {
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    public static String encode(String src) {
        return new String(ENCODER.encode(src.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public static String decode(String tar) {
        return new String(DECODER.decode(tar.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

}
