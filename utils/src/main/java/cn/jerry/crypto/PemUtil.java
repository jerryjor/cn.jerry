package cn.jerry.crypto;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PemUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static String getPEMStringFromKey(Key key) throws IOException {
        StringWriter pemStrWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(pemStrWriter)) {
            pemWriter.writeObject(key);
        }
        return pemStrWriter.toString();
    }

    public static PrivateKey getKeyFromPEMString(String data) throws IOException {
        Reader pemReader = new StringReader(data);

        PEMKeyPair keyPair = null;
        PrivateKeyInfo keyInfo = null;
        try (PEMParser pemParser = new PEMParser(pemReader)) {
            Object obj = pemParser.readObject();
            if (obj instanceof PEMKeyPair) keyPair = (PEMKeyPair) obj;
            if (obj instanceof PrivateKeyInfo) keyInfo = (PrivateKeyInfo) obj;
        }
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (keyPair != null) return converter.getKeyPair(keyPair).getPrivate();
        if (keyInfo != null) return converter.getPrivateKey(keyInfo);
        return null;
    }

    public static Certificate getCertificateFromPEMString(String pem) throws CertificateException, IOException {
        X509Certificate ret;
        List<Provider> providerList = new LinkedList<>(Arrays.asList(Security.getProviders()));
        providerList.add(new BouncyCastleProvider());

        for (Provider provider : providerList) {
            if (null == provider) continue;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8))) {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509", provider);
                Certificate certificate = certFactory.generateCertificate(bis);
                if (certificate instanceof X509Certificate) {
                    ret = (X509Certificate) certificate;
                    return ret;
                }
            }
        }
        return null;
    }

}
