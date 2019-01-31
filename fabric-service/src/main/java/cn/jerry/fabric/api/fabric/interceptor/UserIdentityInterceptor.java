package cn.jerry.fabric.api.fabric.interceptor;

import cn.jerry.fabric.api.fabric.model.SampleUser;
import cn.jerry.fabric.api.fabric.service.UserService;
import cn.jerry.fabric.api.fabric.util.crypto.BouncyCastleSignatureUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.*;

public class UserIdentityInterceptor extends HandlerInterceptorAdapter {
    private static Logger logger = LogManager.getLogger();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String username = request.getHeader("user");
        String signByPrivateKey = request.getHeader("sign");
        // 这个不合理，但是没办法，后面调用fabric服务需要证书和私钥
        String privateKeyStr = request.getHeader("signKey");
        String failMsg = null;
        boolean pass = false;
        try {
            if (username == null || (username = username.trim()).isEmpty()
                    || signByPrivateKey == null || (signByPrivateKey = signByPrivateKey.trim()).isEmpty()
                    || privateKeyStr == null || (privateKeyStr = privateKeyStr.trim()).isEmpty()) {
                failMsg = "no user or sign in header";
                return false;
            }

            PrivateKey key;
            try {
                key = BouncyCastleSignatureUtil.getKeyFromPEMString(new String(
                        DECODER.decode(privateKeyStr.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            } catch (IOException e) {
                logger.error("read string to PrivateKey failed. keyPem string: {}", privateKeyStr, e);
                failMsg = "read string to PrivateKey failed";
                return false;
            }

            UserService userService = new UserService();
            SampleUser user = userService.lookupUser(null, username);
            if (user == null) {
                failMsg = "user not exists";
                return false;
            }
            if (user.getEnrollment() == null || user.getEnrollmentCert() == null) {
                failMsg = "user is not enrolled";
                return false;
            }
            //TODO 验证证书有效期 cert.checkValidity();
            if (user.getEnrollment().getKey() != null) {
                // 验证PrivateKey相同
                if (!Arrays.equals(user.getEnrollment().getKey().getEncoded(), key.getEncoded())) {
                    failMsg = "PrivateKey is not same as it on server";
                    return false;
                }
            } else {
                // 验证PrivateKey和证书是一对
                if (!BouncyCastleSignatureUtil.isKeyCertInPair(key, user.getEnrollmentCert())) {
                    failMsg = "PrivateKey and the certification on server are not in pair";
                    return false;
                }
                // 将PrivateKey补充到enrollment
                user.setEnrollment(new X509Enrollment(key, user.getEnrollment().getCert()));
            }

            // 验证本次请求签名
            String params = buildParamsText(request);
            if (BouncyCastleSignatureUtil.verify(user.getEnrollmentCert(), signByPrivateKey, params)) {
                pass = true;
                return true;
            } else {
                failMsg = "sign is invalid";
                return false;
            }
        } catch (Exception e) {
            // 未知异常，放行吧
            logger.error("Verify sign failed. User: {}", username, e);
            pass = true;
            return true;
        } finally {
            if (!pass) {
                response.setStatus(403);
                response.getWriter().write(failMsg == null ? "verify sign failed" : failMsg);
            }
        }
    }

    private String buildParamsText(HttpServletRequest request) {
        List<String> paramNames = new ArrayList<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            paramNames.add(params.nextElement());
        }
        Collections.sort(paramNames);
        StringBuilder builder = new StringBuilder();
        paramNames.forEach(paramName ->
                builder.append("&").append(paramName).append("=").append(request.getParameter(paramName)));
        return builder.toString();
    }

}
