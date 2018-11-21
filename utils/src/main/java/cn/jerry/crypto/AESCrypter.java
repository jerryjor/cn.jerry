package cn.jerry.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;

/**
 * BouncyCastle是一个开源的加解密解决方案
 */
public class AESCrypter {
    private static final String METHOD = "AES";
    private static final String RNG_NAME = "SHA1PRNG";
    private static final int KEY_LENGTH = 128;
    private static final String ENCODING = "UTF8";
    private static final String KEY_DOMAIN = "@ulebc.io";

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
        return bytes2HexStr(encryptedBytes);
    }

    /**
     * 解密字符串
     *
     * @param strIn 需解密的字符串
     * @return 解密后的字符串
     * @throws Exception
     */
    public String decrypt(String strIn) throws Exception {
        byte[] encryptedBytes = hexStr2Bytes(strIn);
        if (encryptedBytes == null) return "";
        byte[] bytes = decryptCipher.doFinal(encryptedBytes);
        return new String(bytes, ENCODING);
    }

    /**
     * 将byte数组转换为表示16进制值的字符串， 如：byte[]{8,18}转换为：0813
     * 和 byte[] hexStr2Bytes(String) 互为可逆的转换过程
     *
     * @param bytesIn 需要转换的byte数组
     * @return 转换后的字符串
     */
    private String bytes2HexStr(byte[] bytesIn) {
        StringBuilder sb = new StringBuilder();
        for (byte aBytesIn : bytesIn) {
            String hex = Integer.toHexString(aBytesIn & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将表示16进制值的字符串转换为byte数组
     * 和 String bytes2HexStr(byte[]) 互为可逆的转换过程
     *
     * @param hexStr 需要转换的字符串
     * @return 转换后的byte数组
     */
    private byte[] hexStr2Bytes(String hexStr) {
        if (hexStr == null || (hexStr = hexStr.trim()).isEmpty()) return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }
}
