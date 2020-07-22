package cn.jerry.blockchain.fabric.tools;

import cn.jerry.blockchain.fabric.cache.CaAdminCache;
import cn.jerry.blockchain.fabric.conf.OrgConfig;
import cn.jerry.blockchain.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.blockchain.fabric.model.FabricOrg;
import cn.jerry.blockchain.fabric.model.FabricUser;
import cn.jerry.blockchain.util.crypto.DigestUtil;
import cn.jerry.blockchain.util.crypto.X509SignUtil;
import org.apache.http.HttpStatus;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric_ca.sdk.*;
import org.hyperledger.fabric_ca.sdk.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class UserTools {
    private static final Logger log = LoggerFactory.getLogger(UserTools.class);

    private static final OrgConfig ORG_CONF = OrgConfig.getInstance();
    // private static final String ATTR_ORG_MSP = "orgMSP"

    private UserTools() {
        super();
    }

    public static FabricOrg getOrg(FabricUser user) {
        return ORG_CONF.getOrg(user == null ? null : user.getMspId());
    }

    public static boolean existsUser(FabricUser admin, String username) {
        return !lookupCerts(admin, username, true).isEmpty();
    }

    public static FabricUser lookupUser(FabricUser admin, String username, String keyPEM) {
        PrivateKey key = null;
        if (keyPEM != null) {
            try {
                key = X509SignUtil.getKeyFromPEM(keyPEM);
            } catch (IOException e) {
                log.error("Failed to read user {} private key {}.", username, keyPEM, e);
            }
        }
        return lookupUser(admin, username, key);
    }

    public static FabricUser lookupUser(FabricUser admin, String username, PrivateKey key) {
        X509Certificate certOnServer = lookupCert(admin, username);
        if (certOnServer != null) {
            FabricUser user = new FabricUser(username, admin.getMspId(), certOnServer, key);
            fillUserIdentity(admin, user);
            log.info("Found user. {}", user);
            return user;
        } else {
            return null;
        }
    }

    private static X509Certificate lookupCert(FabricUser admin, String username) {
        return getLatestCert(lookupCerts(admin, username, false));
    }

    private static List<X509Certificate> lookupCerts(FabricUser admin, String username, boolean existsQuery) {
        List<X509Certificate> certs = new ArrayList<>();
        FabricOrg org = getOrg(admin);
        try {
            HFCAClient caClient = ClientTool.getHFCAClient(org);
            HFCACertificateRequest certReq = caClient.newHFCACertificateRequest();
            certReq.setEnrollmentID(username);
            if (!existsQuery) {
                certReq.setExpired(false);
                certReq.setRevoked(false);
            }
            HFCACertificateResponse resp = caClient.getHFCACertificates(admin, certReq);
            if (HttpStatus.SC_OK != resp.getStatusCode()) {
                log.warn("Failed to lookup user {} certificate. Status code: {}", username, resp.getStatusCode());
                return certs;
            } else if (resp.getCerts() == null || resp.getCerts().isEmpty()) {
                log.info("No user {} certificate found.", username);
                return certs;
            } else {
                log.info("User {} certificate size {}.", username, resp.getCerts().size());
                resp.getCerts().forEach(c -> {
                    if (c instanceof HFCAX509Certificate) {
                        certs.add(((HFCAX509Certificate) c).getX509());
                    }
                });
            }
        } catch (HFCACertificateException e) {
            log.error("Failed to lookup user {} certificate.", username, e);
        } catch (InitializeCryptoSuiteException e) {
            log.error("Failed to create HFCAClient.", e);
        }
        return certs;
    }

    private static X509Certificate getLatestCert(List<X509Certificate> certs) {
        if (certs == null || certs.isEmpty()) {
            return null;
        }
        X509Certificate cert = null;
        for (X509Certificate c : certs) {
            if (cert == null || cert.getNotAfter().before(c.getNotAfter())) {
                cert = c;
            }
        }
        return cert;
    }

    /**
     * 生成一对新的证书和私钥，新的公钥和私钥和上一次的相同，证书有效期自请求时间开始
     *
     * @param user 用户，必须是已经enroll过的用户
     * @return 含有新的Enrollment的用户
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFCAClient
     * @throws InvalidArgumentException       参数有误，无法执行reenroll请求
     * @throws EnrollmentException            执行enroll失败
     */
    public static FabricUser reenrollUser(FabricUser user)
            throws InitializeCryptoSuiteException, InvalidArgumentException, EnrollmentException {
        FabricOrg org = getOrg(user);
        HFCAClient caClient = ClientTool.getHFCAClient(org);
        user.setEnrollment(caClient.reenroll(user));
        return user;
    }

    /**
     * 生成一对新的证书和私钥，新的公钥和私钥与历史的不同
     *
     * @param org      机构，用于获取机构上缓存的HFCAClient对象
     * @param username 用户名，必须是已经register过的用户
     * @param password 密码，register时设置的密码，用于生成公钥和私钥
     * @return 含有密码sha1及新的Enrollment的用户
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFCAClient
     * @throws InvalidArgumentException       参数有误，无法执行reenroll请求
     * @throws EnrollmentException            执行enroll失败
     */
    public static FabricUser enrollUser(FabricOrg org, String username, String password)
            throws InitializeCryptoSuiteException, InvalidArgumentException, EnrollmentException {
        HFCAClient caClient = ClientTool.getHFCAClient(org);
        FabricUser user = new FabricUser(username, org.getMSPID());
        user.setSecretSha1(DigestUtil.sha1DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        user.setEnrollment(caClient.enroll(user.getName(), password));
        return user;
    }

    /**
     * 注册一个交易用户
     *
     * @param admin    管理员用户
     * @param username 用户名，必须是未经register过的用户
     * @param password 密码，如果不指定，系统会生成一个随机密码并返回
     * @return 含有密码sha1及Enrollment的用户
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFCAClient
     * @throws InvalidArgumentException       参数有误，无法执行register或enroll请求
     * @throws RegistrationException          执行register失败
     * @throws EnrollmentException            执行enroll失败
     */
    public static FabricUser registerClientUser(FabricUser admin, String username, String password)
            throws InitializeCryptoSuiteException, InvalidArgumentException, RegistrationException, EnrollmentException {
        FabricOrg org = getOrg(admin);
        HFCAClient caClient = ClientTool.getHFCAClient(org);
        FabricUser user = new FabricUser(username, org.getMSPID());
        user.setSecretSha1(DigestUtil.sha1DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        RegistrationRequest rr;
        try {
            rr = new RegistrationRequest(username);
        } catch (Exception e) {
            throw new NullPointerException("username may not be null");
        }
        rr.setSecret(password);
        // rr.setAffiliation(admin.getMspId())
        rr.addAttribute(new Attribute(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARROLES, HFCAClient.HFCA_TYPE_CLIENT));
        // rr.addAttribute(new Attribute(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARATTRIBUTES, ATTR_ORG_MSP + "=" + admin.getMspId()))
        caClient.register(rr, admin);
        log.info("register user[{}] succeed.", user.getName());
        user.setEnrollment(caClient.enroll(user.getName(), password));
        log.info("enroll user[{}] succeed.", user.getName());
        fillUserIdentity(admin, user);
        return user;
    }

    /**
     * 注册一个用户管理员
     *
     * @param org       机构，用于获取机构上缓存的HFCAClient对象
     * @param adminName 机构CA服务器的管理员帐号
     * @param adminPw   机构CA服务器的管理员密码
     * @param username  用户名，必须是未经register过的用户
     * @param password  密码，如果不指定，系统会生成一个随机密码并返回；由于系统不存储用户密码，且返回的对象中密码为sha1值，故此参数必填
     * @return 含有密码sha1及Enrollment的用户
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFCAClient
     * @throws InvalidArgumentException       参数有误，无法执行register或enroll请求
     * @throws RegistrationException          执行register失败
     * @throws EnrollmentException            执行enroll失败
     */
    public static FabricUser registerClientAdmin(FabricOrg org, String adminName, String adminPw, String username, String password)
            throws InitializeCryptoSuiteException, InvalidArgumentException, RegistrationException, EnrollmentException {
        return registerAdmin(org, adminName, adminPw, username, password, genDefaultClientAdminAttrs());
    }

    private static Attribute[] genDefaultClientAdminAttrs() {
        return new Attribute[]{
                // 角色：用户管理
                new Attribute(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARROLES, HFCAClient.HFCA_TYPE_CLIENT),
                // 用户管理员属性，默认所有
                new Attribute(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARATTRIBUTES, "*"),
                // 赋予吊销证书的权限
                new Attribute(HFCAClient.HFCA_ATTRIBUTE_HFREVOKER, "true")
        };
    }

    /**
     * 注册一个链码节点
     *
     * @param org       机构，用于获取机构上缓存的HFCAClient对象
     * @param adminName 机构CA服务器的管理员帐号
     * @param adminPw   机构CA服务器的管理员密码
     * @param peerName  节点名，注意遵循规范
     * @return 含有密码sha1及Enrollment的用户
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFCAClient
     * @throws InvalidArgumentException       参数有误，无法执行register或enroll请求
     * @throws RegistrationException          执行register失败
     * @throws EnrollmentException            执行enroll失败
     */
    public static FabricUser registerPeer(FabricOrg org, String adminName, String adminPw, String peerName)
            throws InitializeCryptoSuiteException, InvalidArgumentException, RegistrationException, EnrollmentException {
        Attribute[] attributes = new Attribute[]{
                new Attribute(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARROLES, HFCAClient.HFCA_TYPE_PEER),
        };
        return registerAdmin(org, adminName, adminPw, peerName, peerName, attributes);
    }

    /**
     * 注册一个排序节点
     *
     * @param org         机构，用于获取机构上缓存的HFCAClient对象
     * @param adminName   机构CA服务器的管理员帐号
     * @param adminPw     机构CA服务器的管理员密码
     * @param ordererName 节点名，注意遵循规范
     * @return 含有密码sha1及Enrollment的用户
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFCAClient
     * @throws InvalidArgumentException       参数有误，无法执行register或enroll请求
     * @throws RegistrationException          执行register失败
     * @throws EnrollmentException            执行enroll失败
     */
    public static FabricUser registerOrderer(FabricOrg org, String adminName, String adminPw, String ordererName)
            throws InitializeCryptoSuiteException, InvalidArgumentException, RegistrationException, EnrollmentException {
        Attribute[] attributes = new Attribute[]{
                new Attribute(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARROLES, HFCAClient.HFCA_TYPE_ORDERER),
        };
        return registerAdmin(org, adminName, adminPw, ordererName, ordererName, attributes);
    }

    private static FabricUser registerAdmin(FabricOrg org, String adminName, String adminPw, String username, String password, Attribute[] attributes)
            throws InitializeCryptoSuiteException, InvalidArgumentException, RegistrationException, EnrollmentException {
        // 核对CA管理员信息
        FabricUser caAdmin = CaAdminCache.getInstance().get(org, adminName, adminPw);
        if (caAdmin == null) {
            throw new IllegalArgumentException("admin name/password is not in pair.");
        }
        HFCAClient caClient = ClientTool.getHFCAClient(org);
        FabricUser user = new FabricUser(username, org.getMSPID());
        user.setSecretSha1(DigestUtil.sha1DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        // 组装请求
        RegistrationRequest rr;
        try {
            rr = new RegistrationRequest(username);
        } catch (Exception e) {
            throw new NullPointerException("user name can not be null.");
        }
        rr.setSecret(password);
        // rr.setAffiliation(org.getMSPID())
        for (Attribute attribute : attributes) {
            rr.addAttribute(attribute);
        }
        // 登记
        caClient.register(rr, caAdmin);
        log.info("register user[{}] succeed.", user.getName());
        // 生成证书和私钥，真正生效
        user.setEnrollment(caClient.enroll(user.getName(), password));
        log.info("enroll user[{}] succeed.", user.getName());
        // 填充用户身份信息，返回
        // updateAdminIdentity(caAdmin, username, Arrays.asList(attributes))
        fillUserIdentity(caAdmin, user);
        log.info("fill identity succeed. user: {}", user);
        return user;
    }

    /**
     * 注销用户的当前enrollment
     *
     * @param admin    管理员用户
     * @param username 用户名
     * @param cert     要注销的用户证书
     * @param reason   原因，不能为空
     */
    public static boolean revokeEnrollment(FabricUser admin, String username, X509Certificate cert, String reason) {
        FabricOrg org = getOrg(admin);
        try {
            HFCAClient caClient = ClientTool.getHFCAClient(org);
            X509Enrollment enrollment = new X509Enrollment((PrivateKey) null, X509SignUtil.getPEMFromCert(cert));
            caClient.revoke(admin, enrollment, reason);
            return true;
        } catch (InitializeCryptoSuiteException e) {
            log.error("Failed to create HFClient.", e);
        } catch (RevocationException | InvalidArgumentException e) {
            log.error("Failed to revoke user {} enrollment.", username, e);
        }
        return false;
    }

    /**
     * 注销用户的当前enrollment
     *
     * @param admin    管理员用户
     * @param username 用户名
     * @param reason   原因，不能为空
     */
    public static boolean revokeEnrollmentsExceptLatest(FabricUser admin, String username, String reason) {
        List<X509Certificate> certs = lookupCerts(admin, username, false);
        X509Certificate last = getLatestCert(certs);
        if (last == null) return false;

        FabricOrg org = getOrg(admin);
        try {
            HFCAClient caClient = ClientTool.getHFCAClient(org);
            for (X509Certificate c : certs) {
                if (c.getNotAfter().before(last.getNotAfter())) {
                    X509Enrollment enrollment = new X509Enrollment((PrivateKey) null, X509SignUtil.getPEMFromCert(c));
                    caClient.revoke(admin, enrollment, reason);
                }
            }
            return true;
        } catch (InitializeCryptoSuiteException e) {
            log.error("Failed to create HFClient.", e);
        } catch (RevocationException | InvalidArgumentException e) {
            log.error("Failed to revoke user {} enrollment.", username, e);
        }
        return false;
    }

    /**
     * 修正用户管理员身份
     *
     * @param org       机构，用于获取机构上缓存的HFCAClient对象
     * @param adminName 机构CA服务器的管理员帐号
     * @param adminPw   机构CA服务器的管理员密码
     * @param username  查询的用户
     */
    public static void updateClientAdminIdentity(FabricOrg org, String adminName, String adminPw, String username) {
        // 核对CA管理员信息
        FabricUser caAdmin = CaAdminCache.getInstance().get(org, adminName, adminPw);
        if (caAdmin == null) {
            throw new IllegalArgumentException("admin name/password is not in pair.");
        }
        HFCAIdentity i = lookupIdentity(caAdmin, username);
        if (i == null) {
            return;
        }
        boolean isRevoker = false;
        boolean containsClient = false;
        boolean containsUser = false;
        boolean containsAttr = false;
        for (Attribute a : i.getAttributes()) {
            if (HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARROLES.equals(a.getName()) && HFCAClient.HFCA_TYPE_CLIENT.equals(a.getValue())) {
                containsClient = true;
            } else if (HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARROLES.equals(a.getName()) && HFCAClient.HFCA_TYPE_USER.equals(a.getValue())) {
                containsUser = true;
            } else if (HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARATTRIBUTES.equals(a.getName()) && "*".equals(a.getValue())) {
                containsAttr = true;
            } else if (HFCAClient.HFCA_ATTRIBUTE_HFREVOKER.equals(a.getName()) && "true".equals(a.getValue())) {
                isRevoker = true;
            }
        }
        // 使用 isRevoker == true 判定用户是不是管理员用户，非管理员用户不更新
        if (isRevoker && (!containsClient || containsUser || !containsAttr)) {
            updateAdminIdentity(caAdmin, username, Arrays.asList(genDefaultClientAdminAttrs()));
            log.info("User {} identity is modified.", username);
        }
    }

    private static void fillUserIdentity(FabricUser admin, FabricUser user) {
        HFCAIdentity identity = lookupIdentity(admin, user.getName());
        if (identity != null && identity.getAttributes() != null) {
            user.setRoles(new HashSet<>());
            for (Attribute a : identity.getAttributes()) {
                if (HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARROLES.equals(a.getName())) {
                    user.getRoles().add(a.getValue());
                } else if (HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARATTRIBUTES.equals(a.getName())) {
                    user.setAttributes(a.getValue());
                }
            }
        }
    }

    private static HFCAIdentity lookupIdentity(FabricUser admin, String username) {
        FabricOrg org = getOrg(admin);
        HFCAIdentity i = null;
        try {
            HFCAClient caClient = ClientTool.getHFCAClient(org);
            i = caClient.newHFCAIdentity(username);
            i.read(admin);
        } catch (InvalidArgumentException | IdentityException e) {
            log.error("Failed to lookup user {} identity.", username, e);
        } catch (InitializeCryptoSuiteException e) {
            log.error("Failed to create HFCAClient.", e);
        }
        return i;
    }

    private static void updateAdminIdentity(FabricUser caAdmin, String adminName, List<Attribute> attributes) {
        try {
            HFCAClient caClient = ClientTool.getHFCAClient(getOrg(caAdmin));
            HFCAIdentity i = caClient.newHFCAIdentity(adminName);
            i.read(caAdmin);
            i.setAttributes(attributes);
            i.update(caAdmin);
        } catch (InvalidArgumentException | IdentityException e) {
            log.error("Failed to lookup/update user {} identity.", adminName, e);
        } catch (InitializeCryptoSuiteException e) {
            log.error("Failed to create HFCAClient.", e);
        }
    }
}
