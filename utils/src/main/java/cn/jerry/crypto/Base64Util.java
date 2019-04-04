package cn.jerry.crypto;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.Charset;

public class Base64Util {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String encodeFromString(String src) {
        if (src == null) return null;
        if (src.isEmpty()) return src;

        return Base64.encodeBase64String(src.getBytes(UTF_8));
    }

    public static String encode(byte[] src) {
        if (src == null) return null;
        if (src.length == 0) return "";

        return Base64.encodeBase64String(src);
    }

    public static String decodeToString(String tar) {
        if (tar == null) return null;

        tar = tar.trim();
        if (tar.isEmpty()) return tar;

        return new String(Base64.decodeBase64(tar), UTF_8);
    }

    public static byte[] decode(String tar) {
        if (tar == null) return null;

        tar = tar.trim();
        if (tar.isEmpty()) return new byte[0];

        return Base64.decodeBase64(tar);
    }

}
