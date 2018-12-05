package cn.jerry.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.apache.commons.lang.StringUtils;

public class SHA1WithBase64 {
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    public static String encode(String str) throws NoSuchAlgorithmException {
        if (StringUtils.isBlank(str)) return null;

        MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
        messageDigest.update(str.getBytes());
        return ENCODER.encodeToString(messageDigest.digest());
    }
}
