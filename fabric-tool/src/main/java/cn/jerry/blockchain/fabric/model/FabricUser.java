package cn.jerry.blockchain.fabric.model;

import cn.jerry.blockchain.util.crypto.X509SignUtil;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;

import java.io.IOException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

public class FabricUser implements User, Serializable {

    private final String name;
    private String mspId;
    private Set<String> roles;
    private String attributes;
    private String account;
    private String affiliation;
    private String secretSha1;
    private X509Enrollment enrollment = null;
    private X509Certificate enrollmentCert;

    public FabricUser(String name, String mspId) {
        this.name = name;
        this.mspId = mspId;
    }

    public FabricUser(String name, String mspId, String certPem, PrivateKey key) {
        this(name, mspId);
        setEnrollment(new X509Enrollment(key, certPem));
    }

    public FabricUser(String name, String mspId, X509Certificate cert, PrivateKey key) {
        this(name, mspId, X509SignUtil.getPEMFromCert(cert), key);
        this.enrollmentCert = cert;
    }

    public FabricUser(String name, String mspId, String certPem, String keyPem) throws IOException {
        this(name, mspId, certPem, X509SignUtil.getKeyFromPEM(keyPem));
        setEnrollmentCert(certPem);
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

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
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
        if (enrollment instanceof X509Enrollment) {
            this.enrollment = (X509Enrollment) enrollment;
        } else {
            throw new IllegalArgumentException("Only X509Enrollment is accept.");
        }
        if (enrollment.getCert() != null) {
            setEnrollmentCert(enrollment.getCert());
        }
    }

    public X509Certificate getEnrollmentCert() {
        return this.enrollmentCert;
    }

    private void setEnrollmentCert(String certPem) {
        this.enrollmentCert = X509SignUtil.getX509CertFromPEM(certPem);
    }

    public Date getCertExpireTime() {
        return this.enrollmentCert == null ? null : this.enrollmentCert.getNotAfter();
    }

    @Override
    public String toString() {
        return "FabricUser{" +
                "name='" + name + '\'' +
                ", mspId='" + mspId + '\'' +
                ", roles=" + roles +
                ", attributes=" + attributes +
                ", account='" + account + '\'' +
                ", affiliation='" + affiliation + '\'' +
                '}';
    }
}
