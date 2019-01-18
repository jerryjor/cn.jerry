package cn.jerry.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;

public class BouncyCastleSignatureUtil {
    private static final Provider PROVIDER = new BouncyCastleProvider();
    private static final String SIGNATURE_ALGORITHM_DEFAULT = "SHA256withECDSA";
    private static final String CURVE_NAME_256 = "secp256r1";

    public static String sign(PrivateKey privateKey, byte[] plainData) throws SignatureException, InvalidKeyException {
        byte[] bytes = signToBytes(privateKey, plainData);
        return Base64.toBase64String(bytes);
    }

    private static byte[] signToBytes(PrivateKey privateKey, byte[] plainData) throws SignatureException, InvalidKeyException {
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM_DEFAULT, PROVIDER);
            sig.initSign(privateKey);
            sig.update(plainData);
            return sig.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find Signature with algorithm \"" + privateKey.getAlgorithm() + "\"", e);
        }
    }

    public static boolean verify(Certificate cert, String cryptoText, String plainText) {
        byte[] bytes = Base64.decode(cryptoText);
        return verifyBytes(cert, null, bytes, plainText.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean verify(PublicKey publicKey, String cryptoText, String plainText) {
        byte[] bytes = Base64.decode(cryptoText);
        return verifyBytes(null, publicKey, bytes, plainText.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean verifyBytes(Certificate cert, PublicKey publicKey, byte[] cryptoData, byte[] plainData) {
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM_DEFAULT, PROVIDER);
            if (cert != null) {
                sig.initVerify(cert);
            } else if (publicKey != null) {
                sig.initVerify(publicKey);
            }
            sig.update(plainData);
            return sig.verify(cryptoData);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
