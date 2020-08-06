package cn.jerry.crypto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
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
import org.bouncycastle.util.encoders.Base64;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BCSignUtil {
    private static final Logger logger = LogManager.getLogger();

    private static final Provider SECURITY_PROVIDER_BC = new BouncyCastleProvider();
    private static final String SECURITY_ALGORITHM_EC = "EC";
    private static final String SECURITY_CURVE_NAME = "secp256r1";
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
    private static final String UUID = java.util.UUID.randomUUID().toString();

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

    public static String getPEMStringFromKey(Key key) throws IOException {
        if (key == null) return null;

        StringWriter pemStrWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(pemStrWriter)) {
            pemWriter.writeObject(key);
        }
        return pemStrWriter.toString();
    }

    public static PrivateKey getKeyFromPEMString(String data) throws IOException {
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

    public static X509Certificate getX509CertificateFromPEMString(String pem) throws CertificateException, IOException {
        if (pem == null || pem.isEmpty()) return null;
        byte[] bytes = pem.getBytes(StandardCharsets.UTF_8);

        X509Certificate ret;
        for (Provider provider : Security.getProviders()) {
            if (null == provider) continue;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
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

    public static Principal getCertSubjectDN(X509Certificate cert) {
        if (cert == null) return null;

        return cert.getSubjectDN();
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

    public KeyPair createKeyPair() throws GeneralSecurityException {
        ECGenParameterSpec ecGenSpec = new ECGenParameterSpec(SECURITY_CURVE_NAME);
        KeyPairGenerator g = KeyPairGenerator.getInstance(SECURITY_ALGORITHM_EC, SECURITY_PROVIDER_BC);
        g.initialize(ecGenSpec, new SecureRandom());
        return g.generateKeyPair();
    }

    public X509Certificate createSelfSignedCertificate(CertType certType, KeyPair keyPair, String san)
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

    private X509v3CertificateBuilder createCertBuilder(KeyPair keyPair) {
        X500Name subject = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, UUID)
                .build();

        Calendar notBefore = new GregorianCalendar();
        notBefore.add(Calendar.DAY_OF_MONTH, -1);
        Calendar notAfter = new GregorianCalendar();
        notAfter.add(Calendar.YEAR, 10);

        return new JcaX509v3CertificateBuilder(
                subject,
                new BigInteger(160, new SecureRandom()),
                notBefore.getTime(),
                notAfter.getTime(),
                subject,
                keyPair.getPublic());
    }

    private void addSAN(X509v3CertificateBuilder certBuilder, String san) throws CertIOException {
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
