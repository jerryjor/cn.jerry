package cn.jerry.fabric.api.fabric.controller;

import cn.jerry.fabric.api.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.fabric.api.fabric.model.Result;
import cn.jerry.fabric.api.fabric.model.SampleChaincode;
import cn.jerry.fabric.api.fabric.model.SampleOrg;
import cn.jerry.fabric.api.fabric.service.ChaincodeService;
import cn.jerry.fabric.api.fabric.service.ChannelService;
import cn.jerry.fabric.api.fabric.service.UserService;
import cn.jerry.fabric.api.fabric.util.FileUtil;
import cn.jerry.fabric.api.fabric.util.StringUtils;
import cn.jerry.fabric.api.fabric.util.ThrowableUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.helper.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

@RestController
@RequestMapping("/mgt")
public class ManagementController {
    private static Logger logger = LogManager.getLogger();

    @Autowired
    private UserService userService;
    @Autowired
    private ChaincodeService chaincodeService;

    private static final String FILEPATH = "./chaincode_project_src/";

    /**
     * 部署链码
     *
     * @param chaincode 链码信息
     * @param file      上传的链代码
     * @return
     */
    @RequestMapping(value = {"/deploy", "/deploy.do"})
    public Result<String> deploy(HttpServletRequest request, @RequestParam SampleChaincode chaincode,
            @RequestParam MultipartFile file) {
        Result<String> response = new Result<>();
        long st = System.currentTimeMillis();
        try {
            if (chaincode == null
                    || StringUtils.isBlank(chaincode.getName())
                    || StringUtils.isBlank(chaincode.getVersion())
                    || (TransactionRequest.Type.GO_LANG == chaincode.getLang() && StringUtils.isBlank(chaincode.getInstallPath()))) {
                response.setCode(Result.CODE_PARAM_EMPTY);
                response.setMessage("Param[chaincode] properties NOT enough.");
                return response;
            }
            if (file == null) {
                response.setCode(Result.CODE_PARAM_EMPTY);
                response.setMessage("Param[file] is null.");
                return response;
            }

            logger.info("Deploy chaincode start, chaincode: {}", chaincode);
            //解压上传文件
            String ccSourcePath = FileUtil.unZipFiles(FILEPATH, file);
            chaincode.setSourceBytes(Utils.generateTarGz(new File(ccSourcePath), "src", null));

            SampleOrg org = userService.getDefaultOrg();
            ChannelService channelService = ChannelService.getInstance();
            Channel channel = channelService.getChannel(org, chaincode);
            chaincodeService.install(org, channel, chaincode);
            String txId = chaincodeService.instantiate(org, channel, chaincode, null);

            response.setCode(Result.CODE_SUCCEED);
            response.setMessage("deploy success");
            response.setData(txId);
        } catch (InitializeCryptoSuiteException | ChaincodeEndorsementPolicyParseException | InvalidArgumentException | ProposalException e) {
            response.setCode(Result.CODE_FAILED);
            response.setMessage(ThrowableUtil.findRootCause(e).getMessage());
        } catch (Exception e) {
            logger.error("Deploy chaincode failed, chaincode: {}", chaincode, e);
            response.setCode(Result.CODE_SYS_ERR);
            response.setMessage(ThrowableUtil.findRootCause(e).getMessage());
        } finally {
            //删除本地解压文件
            FileUtil.delAllFile(FILEPATH);
            long et = System.currentTimeMillis();
            logger.info("Deploy chaincode finished, chaincode: {}, result: {}, cost {} ms", chaincode, response,
                    (et - st));
        }
        return response;
    }

    /**
     * 部署链码
     *
     * @param chaincode 链码信息
     * @param file      上传的链代码
     * @return
     */
    @RequestMapping(value = {"/upgrade", "/upgrade.do"})
    public Result<String> upgrade(HttpServletRequest request, @RequestParam SampleChaincode chaincode,
            @RequestParam MultipartFile file) {
        Result<String> response = new Result<>();
        long st = System.currentTimeMillis();
        try {
            if (chaincode == null
                    || StringUtils.isBlank(chaincode.getName())
                    || StringUtils.isBlank(chaincode.getVersion())
                    || (TransactionRequest.Type.GO_LANG == chaincode.getLang() && StringUtils.isBlank(chaincode.getInstallPath()))) {
                response.setCode(Result.CODE_PARAM_EMPTY);
                response.setMessage("Param[chaincode] properties NOT enough.");
                return response;
            }
            if (file == null) {
                response.setCode(Result.CODE_PARAM_EMPTY);
                response.setMessage("Param[file] properties NOT enough.");
                return response;
            }

            logger.info("upgrade chaincode start , chaincode: {}", chaincode);
            //解压上传文件
            String ccSourcePath = FileUtil.unZipFiles(FILEPATH, file);
            chaincode.setSourceBytes(Utils.generateTarGz(new File(ccSourcePath), "src", null));

            SampleOrg org = userService.getDefaultOrg();
            ChannelService channelService = ChannelService.getInstance();
            Channel channel = channelService.getChannel(org, chaincode);
            chaincodeService.install(org, channel, chaincode);
            String txid = chaincodeService.upgrade(org, channel, chaincode);

            response.setCode(Result.CODE_SUCCEED);
            response.setMessage("upgrade success");
            response.setData(txid);
        } catch (InitializeCryptoSuiteException | InvalidArgumentException | ProposalException e) {
            response.setCode(Result.CODE_FAILED);
            response.setMessage(ThrowableUtil.findRootCause(e).getMessage());
        } catch (Exception e) {
            logger.error("Upgrade chaincode exception, chaincode: {}", chaincode, e);
            response.setCode(Result.CODE_SYS_ERR);
            response.setMessage(ThrowableUtil.findRootCause(e).getMessage());
        } finally {
            //删除本地解压文件
            FileUtil.delAllFile(FILEPATH);
            long et = System.currentTimeMillis();
            logger.info("Upgrade Chaincode finished , chaincode: {}, result: {}, cost {} ms", chaincode, response,
                    (et - st));
        }
        return response;
    }

}
