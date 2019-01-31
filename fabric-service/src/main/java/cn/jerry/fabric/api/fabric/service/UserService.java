package cn.jerry.fabric.api.fabric.service;

import cn.jerry.fabric.api.fabric.conf.OrgConfig;
import cn.jerry.fabric.api.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.fabric.api.fabric.model.SampleOrg;
import cn.jerry.fabric.api.fabric.model.SampleUser;
import cn.jerry.fabric.api.fabric.util.crypto.DigestUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric_ca.sdk.Attribute;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric_ca.sdk.exception.RegistrationException;
import org.hyperledger.fabric_ca.sdk.exception.RevocationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

@Service
public class UserService {
    private static Logger log = LogManager.getLogger();

    private static final OrgConfig ORG_CONF = OrgConfig.getInstance();

    public SampleOrg getOrg(String orgName) {
        return ORG_CONF.getSampleOrg(orgName);
    }

    public SampleOrg getDefaultOrg() {
        return ORG_CONF.getDefaultOrg();
    }

    public SampleUser lookupUser(SampleOrg org, String username) {
        if (org != null) return org.getUser(username);

        SampleUser user;
        for (SampleOrg o : ORG_CONF.getSampleOrgs()) {
            user = o.getUser(username);
            if (user != null) return user;
        }

        return null;
    }

    public boolean existsAdmin(String username, String secretSha1) {
        for (SampleOrg org : ORG_CONF.getSampleOrgs()) {
            if (org.getCaAdmin() != null
                    && org.getCaAdmin().getName().equals(username)
                    && org.getCaAdmin().getSecretSha1().equals(secretSha1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成一对新的证书和私钥，新的公钥和私钥和上一次的相同，证书有效期自请求时间开始
     *
     * @param org  机构，用于获取机构上缓存的HFCAClient对象
     * @param user 用户，必须是已经enroll过的用户
     * @return 含有新的Enrollment的用户
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFCAClient
     * @throws InvalidArgumentException       参数有误，无法执行reenroll请求
     * @throws EnrollmentException            执行enroll失败
     */
    public SampleUser reenrollUser(SampleOrg org, SampleUser user)
            throws InitializeCryptoSuiteException, InvalidArgumentException, EnrollmentException {
        HFCAClient caClient = ClientTool.getHFCAClient(org);
        user.setEnrollment(caClient.reenroll(user));
        org.addUser(user);
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
    public SampleUser enrollUser(SampleOrg org, String username, String password)
            throws InitializeCryptoSuiteException, InvalidArgumentException, EnrollmentException {
        HFCAClient caClient = ClientTool.getHFCAClient(org);
        SampleUser user = new SampleUser(username, org.getMSPID());
        user.setSecretSha1(DigestUtil.sha1DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        user.setEnrollment(caClient.enroll(user.getName(), password));
        org.addUser(user);
        return user;
    }

    /**
     * 注册一个交易用户
     *
     * @param org      机构，用于获取机构上缓存的HFCAClient对象
     * @param username 用户名，必须是未经register过的用户
     * @param password 密码，如果不指定，系统会生成一个随机密码并返回
     * @return 含有密码sha1及Enrollment的用户
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFCAClient
     * @throws InvalidArgumentException       参数有误，无法执行register或enroll请求
     * @throws RegistrationException          执行register失败
     * @throws EnrollmentException            执行enroll失败
     */
    public SampleUser registerClientUser(SampleOrg org, String username, String password)
            throws InitializeCryptoSuiteException, InvalidArgumentException, RegistrationException, EnrollmentException {
        HFCAClient caClient = ClientTool.getHFCAClient(org);
        SampleUser user = new SampleUser(username, org.getMSPID());
        user.setSecretSha1(DigestUtil.sha1DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        RegistrationRequest rr;
        try {
            rr = new RegistrationRequest(username);
        } catch (Exception e) {
            throw new NullPointerException("username may not be null");
        }
        rr.setSecret(password);
        caClient.register(rr, org.getCaAdmin());
        log.info("register user[{}] succeed.", user.getName());
        user.setEnrollment(caClient.enroll(user.getName(), password));
        log.info("enroll user[{}] succeed.", user.getName());
        org.addUser(user);
        return user;
    }

    /**
     * 注册一个管理用户
     *
     * @param org      机构，用于获取机构上缓存的HFCAClient对象
     * @param username 用户名，必须是未经register过的用户
     * @param password 密码，如果不指定，系统会生成一个随机密码并返回；由于系统不存储用户密码，且返回的对象中密码为sha1值，故此参数必填
     * @return 含有密码sha1及Enrollment的用户
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFCAClient
     * @throws InvalidArgumentException       参数有误，无法执行register或enroll请求
     * @throws RegistrationException          执行register失败
     * @throws EnrollmentException            执行enroll失败
     */
    public SampleUser registerAdminUser(SampleOrg org, String username, String password)
            throws InitializeCryptoSuiteException, InvalidArgumentException, RegistrationException, EnrollmentException {
        HFCAClient caClient = ClientTool.getHFCAClient(org);
        SampleUser user = new SampleUser(username, org.getMSPID());
        user.setSecretSha1(DigestUtil.sha1DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        RegistrationRequest rr;
        try {
            rr = new RegistrationRequest(username);
        } catch (Exception e) {
            throw new NullPointerException("username may not be null");
        }
        rr.setSecret(password);
        // 角色：用户管理
        rr.addAttribute(new Attribute(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARROLES, HFCAClient.HFCA_TYPE_USER));
        // 赋予吊销证书的权限
        rr.addAttribute(new Attribute(HFCAClient.HFCA_ATTRIBUTE_HFREVOKER, "true"));
        caClient.register(rr, org.getCaAdmin());
        log.info("register user[{}] succeed.", user.getName());
        user.setEnrollment(caClient.enroll(user.getName(), password));
        log.info("enroll user[{}] succeed.", user.getName());
        org.addUser(user);
        return user;
    }

    /**
     * 注销用户的所有历史enrollment
     *
     * @param org      机构，用于获取机构上缓存的HFCAClient对象
     * @param username 用户名，必须是已经register过的用户
     * @param reason   原因，不能为空
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFCAClient
     * @throws InvalidArgumentException       参数有误，无法执行revoke请求
     * @throws RevocationException            执行revoke失败
     */
    public void revokeAllEnrollment(SampleOrg org, String username, String reason)
            throws InitializeCryptoSuiteException, InvalidArgumentException, RevocationException {
        HFCAClient caClient = ClientTool.getHFCAClient(org);
        caClient.revoke(org.getCaAdmin(), username, reason);

        org.removeUser(username);
    }

    /**
     * 注销用户的当前enrollment
     *
     * @param org    机构，用于获取机构上缓存的HFCAClient对象
     * @param user   用户，必须含有Enrollment信息
     * @param reason 原因，不能为空
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFCAClient
     * @throws InvalidArgumentException       参数有误，无法执行revoke请求
     * @throws RevocationException            执行revoke失败
     */
    public void revokeCurrEnrollment(SampleOrg org, SampleUser user, String reason)
            throws InitializeCryptoSuiteException, InvalidArgumentException, RevocationException {
        if (user == null || user.getEnrollmentCert() == null) return;

        X509Certificate cert = user.getEnrollmentCert();
        HFCAClient caClient = ClientTool.getHFCAClient(org);
        caClient.revoke(org.getCaAdmin(), user.getEnrollment(), reason);

        org.removeUser(user.getName());
    }

}
