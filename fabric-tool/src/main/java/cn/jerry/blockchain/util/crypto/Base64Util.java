package cn.jerry.blockchain.util.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Util {
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    public static String encode(String src) {
        if (src == null) return null;
        if (src.isEmpty()) return src;

        return new String(ENCODER.encode(src.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public static String encodeFromBytes(byte[] bytes) {
        if (bytes == null) return null;
        if (bytes.length == 0) return "";

        return new String(ENCODER.encode(bytes), StandardCharsets.UTF_8);
    }

    public static String decode(String tar) {
        if (tar == null) return null;
        tar = tar.trim();
        if (tar.isEmpty()) return tar;

        return new String(DECODER.decode(tar.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public static byte[] decodeToBytes(String tar) {
        if (tar == null) return null;
        tar = tar.trim();
        if (tar.isEmpty()) return new byte[0];

        return DECODER.decode(tar.getBytes(StandardCharsets.UTF_8));
    }

}
