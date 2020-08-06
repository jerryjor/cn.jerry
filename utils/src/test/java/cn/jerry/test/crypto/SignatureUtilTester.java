package cn.jerry.test.crypto;

import cn.jerry.crypto.BCSignUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class SignatureUtilTester {
    private static final String certStr = "-----BEGIN CERTIFICATE-----\n" +
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
            "-----END CERTIFICATE-----\n";
    private static final String privateKeyStr = "-----BEGIN EC PRIVATE KEY-----\n" +
            "MHcCAQEEIAQX9k1ImJIziXggQgW1WB/My6NqGV2w47L0vrRZ9jSEoAoGCCqGSM49" +
            "AwEHoUQDQgAEs9UorJCmtJEF6sSutRF77r4E0xH6D9IwPL+4WE7xLCNORi36GS3E" +
            "Uj42Pe7X/3qdPkGP+wA8JyWkUW56yfKUUQ==\n" +
            "-----END EC PRIVATE KEY-----\n";

    public static void main(String[] args) {
        try {
            Certificate cert = BCSignUtil.getX509CertificateFromPEMString(certStr);
            System.out.println(cert.getType());
            System.out.println(cert.getPublicKey().getAlgorithm());
            PrivateKey key = BCSignUtil.getKeyFromPEMString(privateKeyStr);
            System.out.println(key.getAlgorithm());
            String testStr = "123456";
            String signedStr = BCSignUtil.sign(key, testStr.getBytes(StandardCharsets.UTF_8));
            System.out.println(signedStr);
            System.out.println(BCSignUtil.verify(cert.getPublicKey(), signedStr, testStr));
            System.out.println(BCSignUtil.verify(cert, signedStr, testStr));
        } catch (CertificateException | IOException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
