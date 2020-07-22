package cn.jerry.blockchain.util.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DigestUtil {
    private static final String ALGORITHM_MD5 = "MD5";
    private static final String ALGORITHM_SHA1 = "SHA1";
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    public static String md5DigestAsHex(byte[] bytes) {
        return digestAsHexString(ALGORITHM_MD5, bytes);
    }

    public static String sha1DigestAsHex(byte[] bytes) {
        return digestAsHexString(ALGORITHM_SHA1, bytes);
    }

    private static String digestAsHexString(String algorithm, byte[] bytes) {
        byte[] digestBytes = getDigest(algorithm).digest(bytes);
        return ENCODER.encodeToString(digestBytes);
    }

    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find MessageDigest with algorithm \"" + algorithm + "\"", e);
        }
    }

}
