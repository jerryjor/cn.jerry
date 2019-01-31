package cn.jerry.fabric.api.fabric.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import cn.jerry.fabric.api.fabric.conf.OrgConfig;
import cn.jerry.fabric.api.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.fabric.api.fabric.model.SampleOrg;
import cn.jerry.fabric.api.fabric.model.SampleUser;
import cn.jerry.fabric.api.fabric.service.ClientTool;
import cn.jerry.fabric.api.fabric.util.crypto.BouncyCastleSignatureUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric_ca.sdk.HFCACertificateRequest;
import org.hyperledger.fabric_ca.sdk.HFCACertificateResponse;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.HFCAX509Certificate;
import org.hyperledger.fabric_ca.sdk.exception.HFCACertificateException;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UserCache {
    private static Logger log = LogManager.getLogger();

    private final LoadingCache<String, SampleUser> cache;
    private static UserCache instance = new UserCache();

    private UserCache() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(65536)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build(new UserLoader());
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                new RevokedUserClearingThread(), 30, 120, TimeUnit.SECONDS);
    }

    public static UserCache getInstance() {
        return instance;
    }

    public void put(SampleUser user) {
        cache.put(user.getName(), user);
    }

    public void invalidate(String username) {
        cache.invalidate(username);
    }

    public SampleUser lookup(String username) {
        try {
            return cache.get(username);
        } catch (ExecutionException e) {
            log.error("Lookup user[{}] failed.", username, e);
            return null;
        }
    }

    class UserLoader extends CacheLoader<String, SampleUser> {

        @Override
        public SampleUser load(String username) {
            SampleUser user;
            for (SampleOrg o : OrgConfig.getInstance().getSampleOrgs()) {
                String certOnServer = null;
                try {
                    certOnServer = lookupCert(o, username);
                } catch (HFCACertificateException e) {
                    log.error("Lookup cert[{}] failed.", username, e);
                } catch (InitializeCryptoSuiteException e) {
                    // TODO Create caClient failed. What should I do...
                    log.error("Lookup cert[{}] failed because of construct HFCAClient failed.", username, e);
                }
                if (certOnServer != null) {
                    user = new SampleUser(username, o.getMSPID(), null, certOnServer);
                    o.addUser(user);
                    return user;
                }
            }
            return null;
        }

        private String lookupCert(SampleOrg org, String username) throws InitializeCryptoSuiteException, HFCACertificateException {
            HFCAClient caClient = ClientTool.getHFCAClient(org);
            HFCACertificateRequest certReq = caClient.newHFCACertificateRequest();
            certReq.setEnrollmentID(username);
            HFCACertificateResponse resp = caClient.getHFCACertificates(org.getCaAdmin(), certReq);
            if (resp.getCerts() != null && !resp.getCerts().isEmpty()) {
                return ((HFCAX509Certificate) resp.getCerts().iterator().next()).getPEM();
            } else {
                log.warn("Lookup certificate from server failed. Status code: {}", resp.getStatusCode());
                return null;
            }
        }

    }

    class RevokedUserClearingThread implements Runnable {

        @Override
        public void run() {
            try {
                for (SampleOrg org : OrgConfig.getInstance().getSampleOrgs()) {
                    HFCAClient caClient = ClientTool.getHFCAClient(org);
                    HFCACertificateRequest certReq = caClient.newHFCACertificateRequest();
                    // 5分钟内被注销的证书
                    certReq.setRevokedStart(new Date(System.currentTimeMillis() - 300000L));
                    HFCACertificateResponse resp = caClient.getHFCACertificates(org.getCaAdmin(), certReq);
                    if (resp.getCerts() != null && !resp.getCerts().isEmpty()) {
                        resp.getCerts().forEach(credential -> {
                            try {
                                String pem = ((HFCAX509Certificate) credential).getPEM();
                                X509Certificate certificate = BouncyCastleSignatureUtil.getX509CertificateFromPEMString(pem);
                                String username = BouncyCastleSignatureUtil.getCertSubjectDN(certificate).get("CN");
                                invalidate(username);
                            } catch (Exception e) {
                                log.error("Read certificate failed.", e);
                            }
                        });
                    } else {
                        log.info("No certification is revoked in 5 minutes.");
                    }
                }
            } catch (Exception e) {
                log.error("lookup revoked certificate failed.", e);
            }
        }
    }
}
