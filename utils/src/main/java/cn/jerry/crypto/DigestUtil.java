package cn.jerry.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtil {
    private static final String ALGORITHM_MD5 = "MD5";
    private static final String ALGORITHM_SHA1 = "SHA-1";
    private static final String ALGORITHM_SHA256 = "SHA-256";
    private static final String ALGORITHM_SHA384 = "SHA-384";
    private static final String ALGORITHM_SHA512 = "SHA-512";

    public static String md5(byte[] bytes) {
        return digestAsHexString(ALGORITHM_MD5, bytes);
    }

    public static String sha1(byte[] bytes) {
        return digestAsHexString(ALGORITHM_SHA1, bytes);
    }

    public static String sha256(byte[] bytes) {
        return digestAsHexString(ALGORITHM_SHA256, bytes);
    }

    public static String sha384(byte[] bytes) {
        return digestAsHexString(ALGORITHM_SHA384, bytes);
    }

    public static String sha512(byte[] bytes) {
        return digestAsHexString(ALGORITHM_SHA512, bytes);
    }

    private static String digestAsHexString(String algorithm, byte[] bytes) {
        byte[] digestBytes = getDigest(algorithm).digest(bytes);
        return Base64Util.encode(digestBytes);
    }

    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find MessageDigest with algorithm \"" + algorithm + "\"", e);
        }
    }

}
