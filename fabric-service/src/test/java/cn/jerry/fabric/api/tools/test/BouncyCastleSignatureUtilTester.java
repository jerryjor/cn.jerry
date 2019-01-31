package cn.jerry.fabric.api.tools.test;

import cn.jerry.fabric.api.fabric.conf.OrgConfig;
import cn.jerry.fabric.api.fabric.model.SampleOrg;
import cn.jerry.fabric.api.fabric.util.crypto.BouncyCastleSignatureUtil;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class BouncyCastleSignatureUtilTester {
    public static void main(String[] args) {
        SampleOrg org = OrgConfig.getInstance().getDefaultOrg();

        File certFile;
        try {
            certFile = OrgConfig.getInstance().getOrgAdminSignCert(org.getDomainName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        String certPem;
        try (FileInputStream fisCert = new FileInputStream(certFile)) {
            certPem = IOUtils.toString(fisCert);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        X509Certificate cert = null;
        try {
            cert = BouncyCastleSignatureUtil.getX509CertificateFromPEMString(certPem);
        } catch (CertificateException | IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println(cert.getIssuerDN().getName());
        System.out.println(cert.getSubjectDN().getName());
    }
}
