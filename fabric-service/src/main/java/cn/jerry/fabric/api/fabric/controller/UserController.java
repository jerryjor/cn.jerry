package cn.jerry.fabric.api.fabric.controller;

import cn.jerry.fabric.api.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.fabric.api.fabric.model.Result;
import cn.jerry.fabric.api.fabric.model.SampleOrg;
import cn.jerry.fabric.api.fabric.model.SampleUser;
import cn.jerry.fabric.api.fabric.service.UserService;
import cn.jerry.fabric.api.fabric.util.ThrowableUtil;
import cn.jerry.fabric.api.fabric.util.crypto.BouncyCastleSignatureUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric_ca.sdk.exception.RegistrationException;
import org.hyperledger.fabric_ca.sdk.exception.RevocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

@RestController
@RequestMapping("/user")
public class UserController {
    private static Logger logger = LogManager.getLogger();
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    @Autowired
    private UserService userService;

    @RequestMapping(value = {"/register", "/register.do"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result<HashMap<String, String>> registerUser(@RequestParam(required = false) String orgName, String username,
            String password) {
        Result<HashMap<String, String>> result = new Result<>();

        try {
            SampleOrg org;
            if (orgName == null || (orgName = orgName.trim()).isEmpty()) {
                org = userService.getDefaultOrg();
            } else {
                org = userService.getOrg(orgName);
                if (org == null) {
                    result.setCode(Result.CODE_PARAM_EMPTY);
                    result.setMessage("org[" + orgName + "] not exists.");
                    return result;
                }
            }

            SampleUser user = userService.lookupUser(null, username);
            if (user != null) {
                logger.warn("user {} already registered on org {}.", username, user.getAffiliation());
                result.setCode(Result.CODE_DUPLICATE);
                result.setMessage("user already registered on org " + user.getAffiliation());
                return result;
            }

            String secret = new String(DECODER.decode(password), StandardCharsets.UTF_8);
            user = userService.registerClientUser(org, username, secret);
            result.setCode(Result.CODE_SUCCEED);
            result.setMessage("succeed in msp:" + user.getMspId());

            HashMap<String, String> identities = new HashMap<>();
            identities.put("cert", ENCODER.encodeToString(
                    user.getEnrollment().getCert().getBytes(StandardCharsets.UTF_8)));
            identities.put("key", ENCODER.encodeToString(BouncyCastleSignatureUtil.getPEMStringFromKey(
                    user.getEnrollment().getKey()).getBytes(StandardCharsets.UTF_8)));
            result.setData(identities);
        } catch (RegistrationException e) {
            result.setCode(Result.CODE_DUPLICATE);
            result.setMessage("user may be already registered.");
        } catch (InitializeCryptoSuiteException | InvalidArgumentException e) {
            result.setCode(Result.CODE_FAILED);
            result.setMessage(ThrowableUtil.findRootCause(e).getMessage());
        } catch (Exception e) {
            result.setCode(Result.CODE_SYS_ERR);
            result.setMessage(ThrowableUtil.findRootCause(e).getMessage());
        }
        return result;
    }

    @RequestMapping(value = {"/lost-key", "/lost-key.do"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result<HashMap<String, String>> regenerateCert(HttpServletRequest request,
            @RequestParam(required = false) String orgName, String secret) {
        Result<HashMap<String, String>> result = new Result<>();

        try {
            SampleOrg org;
            if (orgName == null || (orgName = orgName.trim()).isEmpty()) {
                org = userService.getDefaultOrg();
            } else {
                org = userService.getOrg(orgName);
                if (org == null) {
                    result.setCode(Result.CODE_PARAM_EMPTY);
                    result.setMessage("org[" + orgName + "] not exists.");
                    return result;
                }
            }
            String username = request.getHeader("user");
            secret = new String(DECODER.decode(secret), StandardCharsets.UTF_8);
            // revoke原有的enrollment
            userService.revokeAllEnrollment(org, username, "enrollment is lost");
            // 重新生成新的enrollment
            SampleUser user = userService.enrollUser(org, username, secret);
            result.setCode(Result.CODE_SUCCEED);
            result.setMessage("succeed in msp:" + user.getMspId());

            HashMap<String, String> identities = new HashMap<>();
            identities.put("cert", ENCODER.encodeToString(
                    user.getEnrollment().getCert().getBytes(StandardCharsets.UTF_8)));
            identities.put("key", ENCODER.encodeToString(BouncyCastleSignatureUtil.getPEMStringFromKey(
                    user.getEnrollment().getKey()).getBytes(StandardCharsets.UTF_8)));
            result.setData(identities);
        } catch (RevocationException e) {
            result.setCode(Result.CODE_FAILED);
            result.setMessage("revoke history certifications failed.");
        } catch (EnrollmentException e) {
            result.setCode(Result.CODE_FAILED);
            result.setMessage("secret maybe not correct.");
        } catch (InitializeCryptoSuiteException | InvalidArgumentException e) {
            result.setCode(Result.CODE_FAILED);
            result.setMessage(ThrowableUtil.findRootCause(e).getMessage());
        } catch (Exception e) {
            result.setCode(Result.CODE_SYS_ERR);
            result.setMessage(ThrowableUtil.findRootCause(e).getMessage());
        }
        return result;
    }

}
