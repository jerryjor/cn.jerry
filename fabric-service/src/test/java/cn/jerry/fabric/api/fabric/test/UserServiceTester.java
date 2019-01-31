package cn.jerry.fabric.api.fabric.test;

import cn.jerry.fabric.api.fabric.model.SampleOrg;
import cn.jerry.fabric.api.fabric.model.SampleUser;
import cn.jerry.fabric.api.fabric.service.UserService;
import cn.jerry.fabric.api.fabric.util.DateUtil;
import cn.jerry.fabric.api.fabric.util.crypto.BouncyCastleSignatureUtil;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class UserServiceTester {
    private static UserService userService = new UserService();

    private static String orgName = "jerry";
    private static String userName = "tester1";
    private static String secret = "666666";
    private static String cert = "-----BEGIN CERTIFICATE-----\n" +
            "MIICTzCCAfWgAwIBAgIUKn0ILMlTEJH4LvAxz1j8YtDh/+EwCgYIKoZIzj0EAwIw" +
            "ZDELMAkGA1UEBhMCQ04xETAPBgNVBAgTCFNoYW5naGFpMREwDwYDVQQHEwhTaGFu" +
            "Z2hhaTEVMBMGA1UEChMMZGV2LnVsZWJjLmlvMRgwFgYDVQQDEw9jYS5kZXYudWxl" +
            "YmMuaW8wHhcNMTgxMjA0MTE0NDAwWhcNMTkxMjA0MTE0OTAwWjAhMQ8wDQYDVQQL" +
            "EwZjbGllbnQxDjAMBgNVBAMTBXVzZXIyMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcD" +
            "QgAEs9UorJCmtJEF6sSutRF77r4E0xH6D9IwPL+4WE7xLCNORi36GS3EUj42Pe7X" +
            "/3qdPkGP+wA8JyWkUW56yfKUUaOBxzCBxDAOBgNVHQ8BAf8EBAMCB4AwDAYDVR0T" +
            "AQH/BAIwADAdBgNVHQ4EFgQUAl9miZUwQ7rH9HttAbgFqi1SPRowKwYDVR0jBCQw" +
            "IoAgsuSC0svXDM+e5tdVfzVvtvY2CV8EK8c+GD+8HXyZrXMwWAYIKgMEBQYHCAEE" +
            "THsiYXR0cnMiOnsiaGYuQWZmaWxpYXRpb24iOiIiLCJoZi5FbnJvbGxtZW50SUQi" +
            "OiJ1c2VyMiIsImhmLlR5cGUiOiJjbGllbnQifX0wCgYIKoZIzj0EAwIDSAAwRQIh" +
            "AOIEF6AgFfsKoAAkxZFgzK7sfWbJYUjtmVFO4YGN0tdRAiBYhIWYGjSSb1q2ARZu" +
            "5oBr3jqRy+gxiXfxrvuMAT6VRw==\n" +
            "-----END CERTIFICATE-----";
    private static String privateKey = "-----BEGIN EC PRIVATE KEY-----\n" +
            "MHcCAQEEIAQX9k1ImJIziXggQgW1WB/My6NqGV2w47L0vrRZ9jSEoAoGCCqGSM49" +
            "AwEHoUQDQgAEs9UorJCmtJEF6sSutRF77r4E0xH6D9IwPL+4WE7xLCNORi36GS3E" +
            "Uj42Pe7X/3qdPkGP+wA8JyWkUW56yfKUUQ==\n" +
            "-----END EC PRIVATE KEY-----";

    public static void main(String[] args) {
        SampleOrg org = userService.getDefaultOrg();
        // 注册用户
        SampleUser user;
        try {
            user = userService.enrollUser(org, userName, secret);
            System.out.println(user.getEnrollment().getCert());
            Certificate c = BouncyCastleSignatureUtil.getX509CertificateFromPEMString(user.getEnrollment().getCert());
            System.out.println(BouncyCastleSignatureUtil.getPEMStringFromKey(c.getPublicKey()));
            String keyStr = BouncyCastleSignatureUtil.getPEMStringFromKey(user.getEnrollment().getKey());
            System.out.println(keyStr);
            PrivateKey key1 = BouncyCastleSignatureUtil.getKeyFromPEMString(keyStr);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                user = userService.registerClientUser(org, userName, secret);
            } catch (Exception e1) {
                e.printStackTrace();
                return;
            }
        }
        System.out.println(user.getSecretSha1());
        PrivateKey key = user.getEnrollment().getKey();
        System.out.println(key.getAlgorithm());
        try {
            PrivateKey pk = BouncyCastleSignatureUtil.getKeyFromPEMString(privateKey);
            user = new SampleUser(userName, org.getMSPID(), pk, cert);
            // 生成新的证书和私钥用户
            // user = userService.enrollUser(org, userName, secret);
            // 延期当前证书和私钥
            user = userService.reenrollUser(org, user);
            X509Certificate certificate = BouncyCastleSignatureUtil.getX509CertificateFromPEMString(
                    user.getEnrollment().getCert());
            System.out.println("digitalSignature:" + certificate.getKeyUsage()[0]);
            System.out.println("dataEncipherment:" + certificate.getKeyUsage()[3]);
            System.out.println(DateUtil.formatDateTime(certificate.getNotBefore()));
            System.out.println(DateUtil.formatDateTime(certificate.getNotAfter()));
            System.out.println(BouncyCastleSignatureUtil.getPEMStringFromKey(certificate.getPublicKey()));
            System.out.println(BouncyCastleSignatureUtil.getPEMStringFromKey(user.getEnrollment().getKey()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
