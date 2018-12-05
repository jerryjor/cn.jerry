package cn.jerry.crypto;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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

    public static X509Certificate getCertificateFromPEMString(String pem) throws CertificateException, IOException {
        List<Provider> providerList = new LinkedList<>(Arrays.asList(Security.getProviders()));
        providerList.add(new BouncyCastleProvider());
        for (Provider provider : providerList) {
            if (null == provider) continue;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8))) {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509", provider);
                Certificate certificate = certFactory.generateCertificate(bis);
                if (certificate instanceof X509Certificate) {
                    return (X509Certificate) certificate;
                }
            }
        }
        return null;
    }

    public static PrivateKey getPrivateKeyFromPEMString(String pem) throws IOException {
        PrivateKey pk;
        PemReader pr = new PemReader(new StringReader(pem));
        PemObject po = pr.readPemObject();
        PEMParser pp = new PEMParser(new StringReader(pem));
        if (po.getType().equals("PRIVATE KEY")) {
            pk = (new JcaPEMKeyConverter()).getPrivateKey((PrivateKeyInfo) pp.readObject());
        } else {
            PEMKeyPair kp = (PEMKeyPair) pp.readObject();
            pk = (new JcaPEMKeyConverter()).getPrivateKey(kp.getPrivateKeyInfo());
        }
        return pk;
    }

    public static String getPEMStringFromKey(Key key) throws IOException {
        StringWriter pemStrWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(pemStrWriter)) {
            pemWriter.writeObject(key);
        }
        return pemStrWriter.toString();
    }
}
