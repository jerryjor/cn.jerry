package cn.jerry.fabric.api.fabric.model;

import cn.jerry.fabric.api.fabric.util.crypto.BouncyCastleSignatureUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

public class SampleUser implements User, Serializable {
    private static final long serialVersionUID = 8077132186383604355L;
    private static Logger log = LogManager.getLogger();

    private String name;
    private String mspId;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private String secretSha1;
    private Enrollment enrollment = null;
    private X509Certificate enrollmentCert;
    private HFClient client;

    public SampleUser(String name, String mspId) {
        this.name = name;
        this.mspId = mspId;
    }

    public SampleUser(String name, String mspId, PrivateKey key, String cert) {
        this(name, mspId);
        setEnrollment(new X509Enrollment(key, cert));
        setEnrollmentCert(cert);
    }

    public SampleUser(String name, String mspId, File privateKeyFile, File certificateFile) throws IOException {
        this(name, mspId);
        try (FileInputStream fisKey = new FileInputStream(privateKeyFile);
             FileInputStream fisCert = new FileInputStream(certificateFile)) {
            String keyPem = IOUtils.toString(fisKey);
            String certPem = IOUtils.toString(fisCert);
            setEnrollment(new X509Enrollment(
                    BouncyCastleSignatureUtil.getKeyFromPEMString(keyPem), certPem));
            setEnrollmentCert(certPem);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getMspId() {
        return mspId;
    }

    public void setMspId(String mspID) {
        this.mspId = mspID;
    }

    @Override
    public Set<String> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public String getAccount() {
        return this.account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String getAffiliation() {
        return this.affiliation;
    }

    void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getSecretSha1() {
        return secretSha1;
    }

    public void setSecretSha1(String secretSha1) {
        this.secretSha1 = secretSha1;
    }

    @Override
    public Enrollment getEnrollment() {
        return this.enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
        if (enrollment != null && enrollment.getCert() != null) {
            setEnrollmentCert(enrollment.getCert());
        }
    }

    public X509Certificate getEnrollmentCert() {
        return this.enrollmentCert;
    }

    private void setEnrollmentCert(String certPem) {
        try {
            this.enrollmentCert = BouncyCastleSignatureUtil.getX509CertificateFromPEMString(certPem);
        } catch (CertificateException | IOException e) {
            log.error("read pem to X509Certificate failed.", e);
            this.enrollmentCert = null;
        }
    }

    public HFClient getClient() {
        return client;
    }

    public void setClient(HFClient client) {
        this.client = client;
    }
}
