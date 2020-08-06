package cn.jerry.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Util {
    private Base64Util() {
        super();
    }

    public static String encodeFromString(String src) {
        if (src == null) return null;
        if (src.isEmpty()) return src;

        return new String(Base64.getEncoder().encode(src.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public static String encode(byte[] src) {
        if (src == null) return null;
        if (src.length == 0) return "";

        return new String(Base64.getEncoder().encode(src), StandardCharsets.UTF_8);
    }

    public static String decodeToString(String tar) {
        if (tar == null) return null;

        tar = tar.trim();
        if (tar.isEmpty()) return tar;

        return new String(Base64.getDecoder().decode(tar.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public static byte[] decode(String tar) {
        if (tar == null) return null;

        tar = tar.trim();
        if (tar.isEmpty()) return new byte[0];

        return Base64.getDecoder().decode(tar.getBytes(StandardCharsets.UTF_8));
    }

}
