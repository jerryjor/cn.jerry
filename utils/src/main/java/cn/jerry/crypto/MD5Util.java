package cn.jerry.crypto;

import java.security.MessageDigest;
import java.util.Base64;

/**
 * MD5加密工具类
 */
public abstract class MD5Util {
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    public static String digestAsBase64(String pwd) {
        try {
            // 使用平台的默认字符集将此 String 编码为 byte序列，并将结果存储到一个新的 byte数组中
            byte[] btInput = pwd.getBytes();
            // 信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要
            mdInst.update(btInput);
            // 摘要更新之后，通过调用digest（）执行哈希计算，获得密文
            byte[] md = mdInst.digest();
            // 返回经过加密后的字符串
            return ENCODER.encodeToString(md);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
