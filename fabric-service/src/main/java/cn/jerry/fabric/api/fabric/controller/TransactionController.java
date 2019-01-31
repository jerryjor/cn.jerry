package cn.jerry.fabric.api.fabric.controller;

import cn.jerry.fabric.api.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.fabric.api.fabric.model.Result;
import cn.jerry.fabric.api.fabric.model.SampleChaincode;
import cn.jerry.fabric.api.fabric.model.SampleUser;
import cn.jerry.fabric.api.fabric.service.ChannelService;
import cn.jerry.fabric.api.fabric.service.TransactionService;
import cn.jerry.fabric.api.fabric.service.UserService;
import cn.jerry.fabric.api.fabric.util.ThrowableUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.protos.peer.FabricTransaction;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/tx")
public class TransactionController {
    private static Logger logger = LogManager.getLogger();

    @Autowired
    private UserService userService;
    @Autowired
    private TransactionService txService;

    private ChannelService channelService = ChannelService.getInstance();

    /**
     * 执行链码交易
     *
     * @param chaincode 链码信息
     * @param method    交易需调用的链码方法
     * @param args      交易所需的参数
     * @param sync      是否同步调用
     * @return transactionID
     */
    @RequestMapping(value = {"/invoke/{method}", "/invoke/{method}.do"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result<String> doTransaction(HttpServletRequest request, @RequestParam SampleChaincode chaincode,
            @PathVariable(name = "method") String method, @RequestParam String[] args,
            @RequestParam(required = false, defaultValue = "0") String sync) {
        logger.info("Invoke {} requested, args: {}", method, Arrays.toString(args));
        long st = System.currentTimeMillis();
        Result<String> response = new Result<>();
        try {
            if (chaincode == null || chaincode.getId() == null) {
                response.setCode(Result.CODE_PARAM_EMPTY);
                response.setMessage("Param[chaincode] properties NOT enough.");
                return response;
            }
            if (args == null) {
                response.setCode(Result.CODE_PARAM_EMPTY);
                response.setMessage("Param[args] is empty.");
                return response;
            } else {
                for (String arg : args) {
                    if (arg == null) {
                        response.setCode(Result.CODE_PARAM_EMPTY);
                        response.setMessage("Param[args] contains NULL.");
                        return response;
                    }
                }
            }
            SampleUser user = null;
            Channel channel = null;
            try {
                user = userService.lookupUser(null, request.getHeader("user"));
                channel = channelService.getChannel(userService.getOrg(user.getAffiliation()), chaincode);
                String txId = txService.doTransaction(user, channel, chaincode.getId(), method, args, "1".equals(sync));
                response.setCode(Result.CODE_SUCCEED);
                if ("1".equals(sync)) {
                    response.setMessage("Invoke transaction succeed.");
                } else {
                    response.setMessage("Requesting transaction succeed. Please query this transaction 1 minute later for result.");
                }
                response.setData(txId);
            } catch (InitializeCryptoSuiteException | InvalidArgumentException | ProposalException e) {
                response.setCode(Result.CODE_FAILED);
                response.setMessage(ThrowableUtil.findRootCause(e).getMessage());
            } catch (ExecutionException e) {
                String txID = e.getMessage();
                response.setData(txID);
                FabricTransaction.TxValidationCode txCode = txService.queryTxValidationCode(user, channel, txID);
                if (txCode == null) {
                    response.setCode(Result.CODE_IN_PROCESS);
                    response.setMessage("Failed to receive the result of orderers. Please query this transaction 1 minute later for result.");
                } else if (FabricTransaction.TxValidationCode.VALID == txCode) {
                    response.setCode(Result.CODE_SUCCEED);
                    response.setMessage("Invoke transaction succeed.");
                } else {
                    response.setCode(Result.CODE_FAILED);
                    response.setMessage(txCode.name());
                }
            } catch (Exception e) {
                logger.info("Invoke {} failed, args: {}", method, Arrays.toString(args), e);
                response.setCode(Result.CODE_SYS_ERR);
                response.setMessage(ThrowableUtil.findRootCause(e).getMessage());
            }
            return response;
        } finally {
            long et = System.currentTimeMillis();
            logger.info("Invoke {} finished, args: {}, result: {}, cost {} ms", method, Arrays.toString(args),
                    response, (et - st));
        }
    }

    /**
     * 执行链码查询
     *
     * @param chaincode 链码信息
     * @param method    查询需调用的链码方法
     * @param args      查询所需的参数
     * @return latest value
     */
    @RequestMapping(value = {"/query/{method}", "/query/{method}.do"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result<String> doQuery(HttpServletRequest request, @RequestParam SampleChaincode chaincode,
            @PathVariable(name = "method") String method, @RequestParam String[] args,
            @RequestParam(required = false) String txID) {
        logger.info("query {} requested, args: {}", method, Arrays.toString(args));
        long st = System.currentTimeMillis();
        Result<String> response = new Result<>();
        try {
            if (chaincode == null || chaincode.getId() == null) {
                response.setCode(Result.CODE_PARAM_EMPTY);
                response.setMessage("Param[chaincode] properties NOT enough.");
                return response;
            }
            try {
                SampleUser user = userService.lookupUser(null, request.getHeader("user"));
                Channel channel = channelService.getChannel(userService.getOrg(user.getAffiliation()), chaincode);
                if (txID != null) {
                    FabricTransaction.TxValidationCode txCode = txService.queryTxValidationCode(user, channel, txID);
                    if (txCode == null) {
                        response.setCode(Result.CODE_IN_PROCESS);
                        response.setMessage("Transaction is still in process.");
                        return response;
                    } else if (FabricTransaction.TxValidationCode.VALID != txCode) {
                        response.setCode(Result.CODE_FAILED);
                        response.setMessage(txCode.name());
                        return response;
                    }
                }
                String data = txService.doQuery(user, channel, chaincode.getId(), method, args);
                response.setCode(Result.CODE_SUCCEED);
                response.setMessage("query succeed.");
                response.setData(data);
            } catch (Exception e) {
                logger.error("query {} failed, args: {}", method, Arrays.toString(args), e);
                response.setCode(Result.CODE_SYS_ERR);
                response.setMessage(ThrowableUtil.findRootCause(e).getMessage());
            }
            return response;
        } finally {
            long et = System.currentTimeMillis();
            logger.info("query {} finished, args: {}, result: {}, cost {} ms.", method, Arrays.toString(args),
                    response, (et - st));
        }
    }

}
