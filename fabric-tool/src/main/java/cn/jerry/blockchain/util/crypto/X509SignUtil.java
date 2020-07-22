package cn.jerry.blockchain.util.crypto;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.pem.PemObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class X509SignUtil {
    private static final Logger logger = LoggerFactory.getLogger(X509SignUtil.class);

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";

    private static final Provider SECURITY_PROVIDER_BC = new BouncyCastleProvider();
    private static final String SECURITY_ALGORITHM_EC = "EC";
    private static final String SECURITY_CURVE_NAME = "secp256r1";
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
    private static final String UUID = java.util.UUID.randomUUID().toString();
    private static final SecureRandom RAND = new SecureRandom();

    static {
        Security.addProvider(SECURITY_PROVIDER_BC);
    }

    public static String sign(PrivateKey privateKey, String plainText) throws SignatureException, InvalidKeyException {
        if (plainText == null) plainText = "";
        return sign(privateKey, plainText.getBytes(StandardCharsets.UTF_8));
    }

    public static String sign(PrivateKey privateKey, byte[] plainData) throws SignatureException, InvalidKeyException {
        if (plainData == null) plainData = new byte[0];
        byte[] bytes = signToBytes(privateKey, plainData);
        return Base64.toBase64String(bytes);
    }

    private static byte[] signToBytes(PrivateKey privateKey, byte[] plainData) throws SignatureException, InvalidKeyException {
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM, SECURITY_PROVIDER_BC);
            sig.initSign(privateKey);
            sig.update(plainData);
            return sig.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find Signature with algorithm \"" + privateKey.getAlgorithm() + "\"", e);
        }
    }

    public static boolean verify(Certificate cert, String cryptoText, String plainText) {
        if (cryptoText == null) return false;
        if (plainText == null) plainText = "";
        byte[] bytes = Base64.decode(cryptoText);
        return verifyBytes(cert, null, bytes, plainText.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean verify(PublicKey publicKey, String cryptoText, String plainText) {
        if (cryptoText == null) return false;
        if (plainText == null) plainText = "";
        byte[] bytes = Base64.decode(cryptoText);
        return verifyBytes(null, publicKey, bytes, plainText.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean verifyBytes(Certificate cert, PublicKey publicKey, byte[] cryptoData, byte[] plainData) {
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM, SECURITY_PROVIDER_BC);
            if (cert != null) {
                sig.initVerify(cert);
            } else if (publicKey != null) {
                sig.initVerify(publicKey);
            }
            sig.update(plainData);
            return sig.verify(cryptoData);
        } catch (Exception e) {
            logger.error("verify bytes failed.", e);
            return false;
        }
    }

    public static boolean isKeyCertInPair(PrivateKey privateKey, Certificate cert) {
        if (privateKey == null || cert == null) return false;
        String cryptoText;
        try {
            cryptoText = sign(privateKey, SIGNATURE_ALGORITHM);
        } catch (SignatureException | InvalidKeyException e) {
            logger.error("sign text failed.", e);
            return false;
        }
        return verify(cert, cryptoText, SIGNATURE_ALGORITHM);
    }

    public static String getPEMFromKey(Key key) throws IOException {
        if (key == null) return null;

        StringWriter pemStrWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(pemStrWriter)) {
            pemWriter.writeObject(key);
        }
        return pemStrWriter.toString();
    }

    public static PrivateKey getKeyFromPEM(String data) throws IOException {
        if (data == null || data.isEmpty()) return null;

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

    public static String getPEMFromCert(X509Certificate cert) {
        try {
            return getPEM("CERTIFICATE", cert.getEncoded());
        } catch (CertificateEncodingException | IOException e) {
            logger.error("Failed to get encode from cert.", e);
            return null;
        }
    }

    public static X509Certificate getX509CertFromPEM(String pem) {
        if (pem == null || pem.isEmpty()) return null;
        byte[] bytes = pem.getBytes(StandardCharsets.UTF_8);

        X509Certificate ret;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", SECURITY_PROVIDER_BC);
            Certificate certificate = certFactory.generateCertificate(bis);
            if (certificate instanceof X509Certificate) {
                ret = (X509Certificate) certificate;
                return ret;
            }
        } catch (CertificateException e) {
            logger.error("Failed to read pem {}", pem, e);
        } catch (IOException e) {
            logger.error("Failed to open stream.", e);
        }
        return null;
    }

    public static Map<String, String> getCertSubjectDN(X509Certificate cert) {
        Map<String, String> result = new HashMap<>();
        if (cert == null) return result;

        String subjectDN = cert.getSubjectDN().getName();
        String[] subjects = subjectDN.split("[ \t]*,[ \t]*");
        for (String subject : subjects) {
            String[] kv = subject.split("=");
            if (kv.length > 1) {
                result.put(kv[0], kv[1]);
            }
        }

        return result;
    }

    public static BigInteger getCertSN(X509Certificate cert) {
        if (cert == null) return null;

        return cert.getSerialNumber();
    }

    public static AuthorityKeyIdentifier getCertAKI(X509Certificate cert) {
        if (cert == null) return null;

        byte[] extensionValue = cert.getExtensionValue(Extension.authorityKeyIdentifier.getId());
        ASN1OctetString akiOc = ASN1OctetString.getInstance(extensionValue);
        return AuthorityKeyIdentifier.getInstance(akiOc.getOctets());
    }

    public static String createP10PEM(String subject, X509Certificate cert, PrivateKey key) {
        try {
            PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                    new X500Principal("CN=" + subject), cert.getPublicKey());
            JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM);
            csBuilder.setProvider(SECURITY_PROVIDER_BC);
            ContentSigner signer = csBuilder.build(key);
            return getPEMFromP10Cert(p10Builder.build(signer));
        } catch (OperatorCreationException e) {
            logger.error("Failed to create P10 cert.", e);
        }
        return null;
    }

    public static String getPEMFromP10Cert(PKCS10CertificationRequest cert) {
        try {
            return getPEM("CERTIFICATE REQUEST", cert.getEncoded());
        } catch (IOException e) {
            logger.error("Failed to get encode from cert.", e);
            return null;
        }
    }

    private static String getPEM(String type, byte[] bytes) throws IOException {
        PemObject pemCSR = new PemObject(type, bytes);
        StringWriter pemStrWriter = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(pemStrWriter);
        pemWriter.writeObject(pemCSR);
        pemWriter.close();
        String pem = pemStrWriter.toString();
        pemStrWriter.close();
        return pem;
    }

    public static PKCS10CertificationRequest getP10CertFromPEM(String pem) {
        String[] parts = pem.split("\n");
        StringBuilder builder = new StringBuilder(pem.length());
        for (int i = 1; i < parts.length - 1; i++) {
            builder.append(parts[i]);
        }
        try {
            return new PKCS10CertificationRequest(Base64Util.decodeToBytes(builder.toString()));
        } catch (IOException e) {
            logger.error("Failed to read pem {}", pem, e);
        }
        return null;
    }

    public static KeyPair createKeyPair() {
        try {
            ECGenParameterSpec ecGenSpec = new ECGenParameterSpec(SECURITY_CURVE_NAME);
            KeyPairGenerator g = KeyPairGenerator.getInstance(SECURITY_ALGORITHM_EC, SECURITY_PROVIDER_BC);
            g.initialize(ecGenSpec, RAND);
            return g.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    public static X509Certificate createSelfSignedCertificate(CertType certType, KeyPair keyPair, String san)
            throws IOException, OperatorCreationException, CertificateException {
        X509v3CertificateBuilder certBuilder = createCertBuilder(keyPair);

        // Basic constraints
        BasicConstraints constraints = new BasicConstraints(false);
        certBuilder.addExtension(
                Extension.basicConstraints,
                true,
                constraints.getEncoded());
        // Key usage
        KeyUsage usage = new KeyUsage(KeyUsage.keyEncipherment | KeyUsage.digitalSignature);
        certBuilder.addExtension(Extension.keyUsage, false, usage.getEncoded());
        // Extended key usage
        certBuilder.addExtension(
                Extension.extendedKeyUsage,
                false,
                certType.keyUsage().getEncoded());

        if (san != null) {
            addSAN(certBuilder, san);
        }

        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
                .build(keyPair.getPrivate());
        X509CertificateHolder holder = certBuilder.build(signer);

        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        converter.setProvider(new BouncyCastleProvider());
        return converter.getCertificate(holder);
    }

    private static X509v3CertificateBuilder createCertBuilder(KeyPair keyPair) {
        X500Name subject = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, UUID)
                .build();

        Calendar notBefore = new GregorianCalendar();
        notBefore.add(Calendar.DAY_OF_MONTH, -1);
        Calendar notAfter = new GregorianCalendar();
        notAfter.add(Calendar.YEAR, 10);

        return new JcaX509v3CertificateBuilder(
                subject,
                new BigInteger(160, RAND),
                notBefore.getTime(),
                notAfter.getTime(),
                subject,
                keyPair.getPublic());
    }

    private static void addSAN(X509v3CertificateBuilder certBuilder, String san) throws CertIOException {
        ASN1Encodable[] subjectAlternativeNames = new ASN1Encodable[]{new GeneralName(GeneralName.dNSName, san)};
        certBuilder.addExtension(Extension.subjectAlternativeName, false, new DERSequence(subjectAlternativeNames));
    }

    public enum CertType {
        CLIENT, SERVER;

        ExtendedKeyUsage keyUsage() {
            KeyPurposeId[] kpid = new KeyPurposeId[]{KeyPurposeId.id_kp_clientAuth};
            if (this.ordinal() == 1) {
                kpid[0] = KeyPurposeId.id_kp_serverAuth;
            }
            return new ExtendedKeyUsage(kpid);
        }
    }

}
