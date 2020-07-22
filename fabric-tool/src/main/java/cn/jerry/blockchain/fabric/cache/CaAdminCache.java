package cn.jerry.blockchain.fabric.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import cn.jerry.blockchain.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.blockchain.fabric.model.FabricOrg;
import cn.jerry.blockchain.fabric.model.FabricUser;
import cn.jerry.blockchain.fabric.tools.ClientTool;
import cn.jerry.blockchain.util.crypto.DigestUtil;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class CaAdminCache {
    private static final Logger log = LoggerFactory.getLogger(CaAdminCache.class);

    private final Cache<String, FabricUser> cache;
    private static final CaAdminCache instance = new CaAdminCache();

    private CaAdminCache() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(256)
                .expireAfterWrite(360, TimeUnit.DAYS)
                .build();
    }

    public static CaAdminCache getInstance() {
        return instance;
    }

    public FabricUser get(FabricOrg org, String adminName, String adminPw) {
        String key = genKey(org.getName(), adminName);
        FabricUser user = cache.getIfPresent(key);
        if (user == null) {
            try {
                user = setupCaAdmin(org, adminName, adminPw);
                cache.put(key, user);
            } catch (InitializeCryptoSuiteException e) {
                log.error("Failed to initialize ca client.", e);
            } catch (InvalidArgumentException e) {
                log.error("Argument invalid. Maybe username and password are not correct.", e);
            } catch (EnrollmentException e) {
                log.error("Failed to enroll user.", e);
            }
        } else if (!user.getSecretSha1().equals(genSha1(adminPw))) {
            log.error("Admin password {} is not correct.", adminPw);
            return null;
        }
        return user;
    }

    public void clear() {
        cache.invalidateAll();
    }

    private FabricUser setupCaAdmin(FabricOrg org, String adminName, String adminPw)
            throws InitializeCryptoSuiteException, EnrollmentException, InvalidArgumentException {
        FabricUser caAdmin = new FabricUser(adminName, org.getMSPID());
        caAdmin.setSecretSha1(genSha1(adminPw));

        HFCAClient ca = ClientTool.getHFCAClient(org);
        caAdmin.setEnrollment(ca.enroll(adminName, adminPw));

        return caAdmin;
    }

    private String genKey(String orgName, String adminName) {
        return adminName + "@" + orgName;
    }

    private String genSha1(String str) {
        return DigestUtil.sha1DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
    }
}