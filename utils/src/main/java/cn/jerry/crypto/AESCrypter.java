package cn.jerry.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

/**
 * BouncyCastle是一个开源的加解密解决方案
 */
public class AESCrypter {
    private static final String METHOD = "AES";
    private static final String RNG_NAME = "SHA1PRNG";
    private static final int KEY_LENGTH = 128;
    private static final String ENCODING = "UTF8";
    private static final String KEY_DOMAIN = "@jerry.cn";

    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private Cipher encryptCipher; // 加密工具
    private Cipher decryptCipher; // 解密工具

    /**
     * 默认构造方法，使用默认密钥
     *
     * @throws Exception
     */
    public AESCrypter(String keyStr) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Key key = getKey(keyStr);

        encryptCipher = Cipher.getInstance(METHOD, "BC");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);

        decryptCipher = Cipher.getInstance(METHOD, "BC");
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
    }

    /**
     * @return 生成的密钥
     * @throws Exception
     */
    private Key getKey(String keyStr) throws Exception {
        SecureRandom secureRandom = SecureRandom.getInstance(RNG_NAME);
        secureRandom.setSeed((keyStr + KEY_DOMAIN).getBytes());
        KeyGenerator keyGen = KeyGenerator.getInstance(METHOD);
        keyGen.init(KEY_LENGTH, secureRandom);
        SecretKey secretKey = keyGen.generateKey();
        return new SecretKeySpec(secretKey.getEncoded(), METHOD);
    }

    /**
     * 加密字符串
     *
     * @param strIn 需加密的字符串
     * @return 加密后的字符串
     * @throws Exception
     */
    public String encrypt(String strIn) throws Exception {
        byte[] bytes = strIn.getBytes(ENCODING);
        byte[] encryptedBytes = encryptCipher.doFinal(bytes);
        return ENCODER.encodeToString(encryptedBytes);
    }

    /**
     * 解密字符串
     *
     * @param strIn 需解密的字符串
     * @return 解密后的字符串
     * @throws Exception
     */
    public String decrypt(String strIn) throws Exception {
        byte[] encryptedBytes = DECODER.decode(strIn);
        if (encryptedBytes == null) return "";
        byte[] bytes = decryptCipher.doFinal(encryptedBytes);
        return new String(bytes, ENCODING);
    }

}
