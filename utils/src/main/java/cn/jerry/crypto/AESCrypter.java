package cn.jerry.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * BouncyCastle是一个开源的加解密解决方案
 */
public class AESCrypter {
    private static final String METHOD = "AES/GCM/NoPadding";
    private static final String RNG_NAME = "SHA1PRNG";
    private static final int KEY_LENGTH = 128;
    private static final String KEY_DOMAIN = "@jerry.cn";

    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private final Cipher encryptCipher; // 加密工具
    private final Cipher decryptCipher; // 解密工具

    /**
     * 默认构造方法，使用默认密钥
     *
     * @throws GeneralSecurityException 购建失败
     */
    public AESCrypter(String keyStr) throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        Key key = getKey(keyStr);
        IvParameterSpec iv = new IvParameterSpec(key.getEncoded(), 0, 16);

        encryptCipher = Cipher.getInstance(METHOD, "BC");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key, iv);

        decryptCipher = Cipher.getInstance(METHOD, "BC");
        decryptCipher.init(Cipher.DECRYPT_MODE, key, iv);
    }

    /**
     * @return 生成的密钥
     * @throws NoSuchAlgorithmException 找不到对应的算法
     */
    private Key getKey(String keyStr) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstance(RNG_NAME);
        secureRandom.setSeed((keyStr + KEY_DOMAIN).getBytes());
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_LENGTH, secureRandom);
        SecretKey secretKey = keyGen.generateKey();
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    /**
     * 加密字符串
     *
     * @param strIn 需加密的字符串
     * @return 加密后的字符串
     * @throws GeneralSecurityException 加密失败
     */
    public String encrypt(String strIn) throws GeneralSecurityException {
        byte[] bytes = strIn.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = encryptCipher.doFinal(bytes);
        return ENCODER.encodeToString(encryptedBytes);
    }

    /**
     * 解密字符串
     *
     * @param strIn 需解密的字符串
     * @return 解密后的字符串
     * @throws GeneralSecurityException 解密失败
     */
    public String decrypt(String strIn) throws GeneralSecurityException {
        byte[] encryptedBytes = DECODER.decode(strIn);
        if (encryptedBytes == null) return "";
        byte[] bytes = decryptCipher.doFinal(encryptedBytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
