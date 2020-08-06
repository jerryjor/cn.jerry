package cn.jerry.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class Sha256Util {
    public static final String SHA_256 = "SHA-256";
    private static final int SALT_LENGTH = 16;

    private Sha256Util() {
        super();
    }

    public static String genSha256(String str) {
        return genSha256(str.getBytes(StandardCharsets.UTF_8));
    }

    public static String genSha256(byte[] bytes) {
        byte[] salt = getRandomSalt();
        return genSha256(bytes, salt);
    }

    private static String genSha256(byte[] bytes, byte[] salt) {
        MessageDigest digest = getDigest();
        byte[] dBytes = digest.digest(appendBytes(bytes, salt));
        byte[] result = appendSalt(dBytes, salt);
        return new String(Base64.getEncoder().encode(result), StandardCharsets.UTF_8);
    }

    private static byte[] appendBytes(byte[] bytes, byte[] salt) {
        byte[] contacted = new byte[bytes.length + salt.length];
        System.arraycopy(bytes, 0, contacted, 0, bytes.length);
        System.arraycopy(salt, 0, contacted, bytes.length, salt.length);
        return contacted;
    }

    private static byte[] appendSalt(byte[] digest, byte[] salt) {
        byte[] result = new byte[(SALT_LENGTH << 1) + SALT_LENGTH];
        int t;
        for (int i = 0; i < SALT_LENGTH; i++) {
            t = i << 1;
            result[t + i] = digest.length <= t ? 0 : digest[t];
            t++;
            if ((i & 1) == 0) {
                result[t + i] = salt[i];
                result[t + i + 1] = digest.length <= t ? 0 : digest[t];
            } else {
                result[t + i] = digest.length <= t ? 0 : digest[t];
                result[t + i + 1] = salt[i];
            }
        }
        return result;
    }

    private static byte[] getRandomSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    public static boolean validSha256(String str, String sha256) {
        return validSha256(str.getBytes(StandardCharsets.UTF_8), sha256);
    }

    public static boolean validSha256(byte[] bytes, String sha256) {
        byte[] result = Base64.getDecoder().decode(sha256.getBytes(StandardCharsets.UTF_8));
        byte[] salt = getSalt(result);
        return genSha256(bytes, salt).equals(sha256);
    }

    private static byte[] getSalt(byte[] sha256) {
        byte[] salt = new byte[SALT_LENGTH];
        int t;
        for (int i = 0; i < SALT_LENGTH; i++) {
            t = i << 1;
            if ((i & 1) == 0) {
                t = t + i + 1;
            } else {
                t = t + i + 2;
            }
            salt[i] = sha256.length <= t ? 0 : sha256[t];
        }
        return salt;
    }

    private static MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance(SHA_256);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Could not find MessageDigest with algorithm \"" + SHA_256 + "\"", e);
        }
    }

}
