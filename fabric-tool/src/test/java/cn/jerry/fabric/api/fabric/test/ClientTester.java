package cn.jerry.fabric.api.fabric.test;

import cn.jerry.blockchain.fabric.cache.CaAdminCache;
import cn.jerry.blockchain.fabric.conf.OrgConfig;
import cn.jerry.blockchain.fabric.model.FabricOrg;
import cn.jerry.blockchain.fabric.model.FabricUser;
import cn.jerry.blockchain.fabric.tools.ClientTool;
import cn.jerry.blockchain.fabric.tools.UserTools;
import cn.jerry.blockchain.util.crypto.Base64Util;
import cn.jerry.blockchain.util.crypto.X509SignUtil;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.hyperledger.fabric_ca.sdk.Attribute;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.HFCAIdentity;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.*;

public class ClientTester {
    private static String adminName = "admin4";
    private static String adminSec = "admin4";
    private static String adminCert = "-----BEGIN CERTIFICATE-----\n" +
            "MIICUDCCAfegAwIBAgIUNfI35/TfcFU+H7FJLHWdtckA3hQwCgYIKoZIzj0EAwIw\n" +
            "ZDELMAkGA1UEBhMCQ04xETAPBgNVBAgTCFNoYW5naGFpMREwDwYDVQQHEwhTaGFu\n" +
            "Z2hhaTEVMBMGA1UEChMMZGV2LnVsZWJjLmlvMRgwFgYDVQQDEw9jYS5kZXYudWxl\n" +
            "YmMuaW8wHhcNMjAwNjIzMTA0MTAwWhcNMjEwNjIzMTA0NjAwWjAiMQ8wDQYDVQQL\n" +
            "EwZjbGllbnQxDzANBgNVBAMTBmFkbWluNDBZMBMGByqGSM49AgEGCCqGSM49AwEH\n" +
            "A0IABG6F+vv+5wCudUoXelG9xDsqRdDwmBNXOeTrVwnFDhcXx4UytAr1uSompUBB\n" +
            "Ttw1xUub9XBFxFOpAo6R0CJ3H/6jgcgwgcUwDgYDVR0PAQH/BAQDAgeAMAwGA1Ud\n" +
            "EwEB/wQCMAAwHQYDVR0OBBYEFDvJGOcAb4OHz9exfnuHZcPaTQbxMCsGA1UdIwQk\n" +
            "MCKAILLkgtLL1wzPnubXVX81b7b2NglfBCvHPhg/vB18ma1zMFkGCCoDBAUGBwgB\n" +
            "BE17ImF0dHJzIjp7ImhmLkFmZmlsaWF0aW9uIjoiIiwiaGYuRW5yb2xsbWVudElE\n" +
            "IjoiYWRtaW40IiwiaGYuVHlwZSI6ImNsaWVudCJ9fTAKBggqhkjOPQQDAgNHADBE\n" +
            "AiBnBASS0V/anpHyeJRqlXpKL6UwSUnXcMoKtp8T6kdmlAIgB5Y823BLsC7OS1x2\n" +
            "BCtBoFrVFfUzhjAV9ZE0/8E6Vq8=\n" +
            "-----END CERTIFICATE-----";
    private static String adminKey = "-----BEGIN EC PRIVATE KEY-----\n" +
            "MHcCAQEEIHcCFDQH/b9VX6lPQhJ3E/pb5PJKYQWcQpzf2nvrcTr8oAoGCCqGSM49\n" +
            "AwEHoUQDQgAEboX6+/7nAK51Shd6Ub3EOypF0PCYE1c55OtXCcUOFxfHhTK0CvW5\n" +
            "KialQEFO3DXFS5v1cEXEU6kCjpHQIncf/g==\n" +
            "-----END EC PRIVATE KEY-----";

    public static void main(String[] args) {
        // P10
        String certPEM = "-----BEGIN CERTIFICATE REQUEST-----" +
                "\nMIHNMHYCAQAwFDESMBAGA1UEAwwJZmluYW5jZV9hMFkwEwYHKoZIzj0CAQYIKoZI" +
                "\nzj0DAQcDQgAEphtX5fLrsUPTxBYMZN7IyX4+pT9+QTKMspnuU8jzSRYIb+doWMhr" +
                "\no/41xErt4RnE+lbRrv+AWnY52VAigjGshKAAMAoGCCqGSM49BAMCA0cAMEQCID6M" +
                "\n9w6dxpw+aJvytQiNIiLxsxYgXUvYJ8hOeVYZX25nAiAqsZvP4+1ZnKsNUslnA7/J" +
                "\n2b/5eUVCBVwHPxGEflgsiQ==" +
                "\n-----END CERTIFICATE REQUEST-----";
        try {
            X509Certificate cert = X509SignUtil.getX509CertFromPEM(adminCert);
            PrivateKey key = X509SignUtil.getKeyFromPEM(adminKey);
//            System.out.println(X509SignUtil.getPEMFromCert(cert));
//            System.out.println(cert.getNotAfter());
//            System.out.println(cert.getSubjectX500Principal().getName());
//            System.out.println(cert.getSubjectDN().getName());

//            String p10PEM = X509SignUtil.createP10PEM(adminName, cert, key);
//            System.out.println(p10PEM);

//            PKCS10CertificationRequest p10Cert = X509SignUtil.getP10CertFromPEM(p10PEM);
//            System.out.println(p10Cert.getSubject());

            FabricOrg org = OrgConfig.getInstance().getDefaultOrg();
            HFCAClient client = ClientTool.getHFCAClient(org);
            FabricUser caAdmin = CaAdminCache.getInstance().get(org, "admin", Base64Util.decode("YWRtaW5wdw=="));

//            FabricUser admin1 = UserTools.registerClientAdmin(org, "admin", Base64Util.decode("YWRtaW5wdw=="),
//                    adminName, adminSec);
//            System.out.println(admin1.getEnrollment().getCert());
//            System.out.println(X509SignUtil.getPEMFromKey(admin1.getEnrollment().getKey()));
//            System.out.println(admin1);
            FabricUser admin1 = new FabricUser(adminName, org.getMSPID(),
                    X509SignUtil.getX509CertFromPEM(adminCert),
                    X509SignUtil.getKeyFromPEM(adminKey));

//            HFCAIdentity i = UserTools.updateClientAdminIdentity(org, "admin", Base64Util.decode("YWRtaW5wdw=="), "admin3");
//            System.out.print(i.getEnrollmentId() + " : " + i.getType() + " [");
//            i.getAttributes().forEach(a -> {
//                System.out.print(a.getName() + " : " + a.getValue() + ", ");
//            });
//            System.out.println("]");

            FabricUser user1 = UserTools.registerClientUser(admin1, "user5", "user5");
            System.out.println(user1.getEnrollment().getCert());
            System.out.println(X509SignUtil.getPEMFromKey(user1.getEnrollment().getKey()));
            System.out.println(user1);

//            FabricUser user1 = UserTools.reenrollUser(admin1);
//            System.out.println(user1.getEnrollment().getCert());
//            System.out.println(X509SignUtil.getPEMFromKey(user1.getEnrollment().getKey()));

//            FabricUser user1 = UserTools.enrollUser(org, adminName, adminSec);
//            System.out.println(user1.getEnrollment().getCert());
//            System.out.println(X509SignUtil.getPEMFromKey(user1.getEnrollment().getKey()));

//            System.out.println(UserTools.existsUser(admin1, "admin1"));
//            FabricUser user = UserTools.lookupUser(admin1, "admin2", key);
//            System.out.println(user);
//            System.out.println(user.getEnrollment().getCert());
//            System.out.println(X509SignUtil.getPEMFromKey(user.getEnrollment().getKey()));

//            boolean revoked = UserTools.revokeAllEnrollment(admin1, adminName, adminSec);
//            System.out.println("revoked = " + revoked);

//            FabricUser admin1 = UserTools.enrollUser(org, adminName, adminSec);
//            System.out.println(admin1.getEnrollment().getCert());
//            System.out.println(X509SignUtil.getPEMFromKey(admin1.getEnrollment().getKey()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
