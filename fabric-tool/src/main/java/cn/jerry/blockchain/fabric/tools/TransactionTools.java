package cn.jerry.blockchain.fabric.tools;

import cn.jerry.blockchain.fabric.cache.PeerHealthyCache;
import cn.jerry.blockchain.fabric.conf.ChaincodeConfig;
import cn.jerry.blockchain.fabric.conf.OrgConfig;
import cn.jerry.blockchain.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.blockchain.fabric.model.FabricUser;
import cn.jerry.blockchain.util.GzipUtil;
import cn.jerry.blockchain.util.JsonUtil;
import cn.jerry.blockchain.util.ThrowableUtil;
import org.hyperledger.fabric.protos.peer.FabricTransaction;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TransactionTools {
    private static final Logger log = LoggerFactory.getLogger(TransactionTools.class);

    private static final ChaincodeConfig CC_CONF = ChaincodeConfig.getInstance();
    // 本常量与链码中代码逻辑有关，不可以修改
    public static final String EXPECTED_EVENT_NAME = "event";
    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    // 本常量与链码中代码逻辑有关，不可以修改
    private static final String EXPECTED_RESULT_NAME = "result";
    private static final byte[] EXPECTED_RESULT_DATA = ":)".getBytes(UTF_8);

    private TransactionTools() {
        super();
    }

    /**
     * 执行链码的交易
     *
     * @param user        用户，必须含有完整的Enrollment
     * @param chaincodeID 链码ID
     * @param lang        链码语言
     * @param method      调用的交易方法
     * @param args        该交易方法所需的参数，有顺序
     * @param sync        是否等待交易结果后同步返回
     * @return 交易id
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     * @throws IOException                    读取必要的文件失败，可能缺少配置文件
     * @throws TransactionException           初始化通道失败
     * @throws InvalidArgumentException       构建通道时节点等信息有误/参数有误，无法执行请求
     * @throws ProposalException              请求交易失败
     * @throws ExecutionException             获取交易结果失败
     */
    public static String doTransaction(FabricUser user, ChaincodeID chaincodeID, TransactionRequest.Type lang,
            String method, String[] args, boolean sync, boolean gzip) throws InitializeCryptoSuiteException,
            InvalidArgumentException, ProposalException, ExecutionException, IOException, TransactionException {

        HFClient client = ClientTool.getUserHFClient(user);
        Channel channel = ChannelTools.getChannel(OrgConfig.getInstance().getOrg(user.getMspId()), chaincodeID);
        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setChaincodeLanguage(lang);
        transactionProposalRequest.setFcn(method);
        transactionProposalRequest.setProposalWaitTime(CC_CONF.getProposalTimeout());
        if (gzip) {
            ArrayList<String> zippedArgs = new ArrayList<>();
            for (String arg : args) {
                zippedArgs.add(GzipUtil.gzip(arg));
            }
            transactionProposalRequest.setArgs(zippedArgs);
        } else {
            transactionProposalRequest.setArgs(args);
        }

        Map<String, byte[]> txMap = new HashMap<>();
        txMap.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        txMap.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        // These two should be returned, see chaincode why.
        txMap.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);
        txMap.put(EXPECTED_RESULT_NAME, EXPECTED_RESULT_DATA);
        // For GO_LANG. This should be returned see chaincode why.
        if (chaincodeID.getPath() != null) {
            txMap.put("rc", "200".getBytes(UTF_8));
        }
        transactionProposalRequest.setTransientMap(txMap);
        transactionProposalRequest.setUserContext(user);
        log.info("Sending proposal to all peers. chaincode: {}, method: {}", chaincodeID.getName(), method);

        int successful = 0;
        int failed = 0;
        String txId = null;
        String failMessage = null;
        final Collection<ProposalResponse> successfulProposals = new LinkedList<>();
        Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest);
        for (ProposalResponse response : transactionPropResp) {
            txId = response.getTransactionID();
            if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                successful++;
                successfulProposals.add(response);
                log.info("Successful transaction proposal response Txid: {} from peer {}. chaincode: {}, method: {}",
                        txId, response.getPeer().getName(), chaincodeID.getName(), method);
            } else {
                failed++;
                failMessage = cutFailedMsg(response.getMessage());
                log.error("Fail transaction proposal response Txid: {} from peer {}, {}. chaincode: {}, method: {}",
                        txId, response.getPeer().getName(), failMessage, chaincodeID.getName(), method);
            }
        }
        log.info("Received {} transaction proposal responses. Successful+verified: {}. Failed: {}. chaincode: {}, method: {}",
                transactionPropResp.size(), successful, failed, chaincodeID.getName(), method);
        if (successful == 0) {
            log.error("Not enough endorsers for {}.{}({}), {} failed with: {}. user cert expire time: {}",
                    chaincodeID.getName(), method, args == null || args.length == 0 ? null : args[0], failed, failMessage,
                    user.getCertExpireTime());
            throw new ProposalException("One or more instantiate proposal response failed: " + failMessage);
        }

        log.info("Sending transaction[{}] to orderers. chaincode: {}, method: {}", txId, chaincodeID.getName(), method);
        CompletableFuture<BlockEvent.TransactionEvent> future = channel.sendTransaction(successfulProposals);
        if (sync) {
            try {
                future.get(CC_CONF.getInvokeTimeout(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.warn("Sending transaction to orderers is interrupted. txId: {}. chaincode: {}, method: {}",
                        txId, chaincodeID.getName(), method);
                Thread.currentThread().interrupt();
                throw new ExecutionException(txId, e);
            } catch (TimeoutException e) {
                log.warn("Sending transaction to orderers is timeout. txId: {}. chaincode: {}, method: {}",
                        txId, chaincodeID.getName(), method);
                throw new ExecutionException(txId, e);
            } catch (ExecutionException e) {
                log.info("Failed to send proposals to orderers. txId: {}. chaincode: {}, method: {}", txId,
                        chaincodeID.getName(), method, e);
                throw new ExecutionException(txId, ThrowableUtil.findRootCause(e));
            }
        }
        return txId;
    }

    private static String cutFailedMsg(String message) {
        if (message == null || message.isEmpty()) return "";
        int start = message.lastIndexOf("description=");
        if (start == -1) return message;
        start = start + "description=".length();
        int end = message.indexOf(",", start);
        return end == -1 ? message.substring(start) : message.substring(start, end);
    }

    /**
     * 执行链码查询
     *
     * @param user        用户，必须含有完整的Enrollment
     * @param chaincodeID 链码ID
     * @param lang        链码语言
     * @param method      调用的查询方法
     * @param args        该查询方法所需的参数，有顺序
     * @return 查询结果，payload
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     * @throws IOException                    读取必要的文件失败，可能缺少配置文件
     * @throws TransactionException           初始化通道失败
     * @throws InvalidArgumentException       构建通道时节点等信息有误/参数有误，无法执行请求
     * @throws ProposalException              查询失败
     */
    public static String doQuery(FabricUser user, ChaincodeID chaincodeID, TransactionRequest.Type lang,
            String method, String[] args, boolean gzip) throws InitializeCryptoSuiteException, InvalidArgumentException,
            ProposalException, IOException, TransactionException {
        HFClient client = ClientTool.getUserHFClient(user);
        Channel channel = ChannelTools.getChannel(OrgConfig.getInstance().getOrg(user.getMspId()), chaincodeID);
        QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
        Map<String, byte[]> txMap = new HashMap<>();
        txMap.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        txMap.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
        queryByChaincodeRequest.setTransientMap(txMap);
        queryByChaincodeRequest.setChaincodeID(chaincodeID);
        queryByChaincodeRequest.setChaincodeLanguage(lang);
        queryByChaincodeRequest.setFcn(method);
        if (gzip) {
            ArrayList<String> zippedArgs = new ArrayList<>();
            for (String arg : args) {
                zippedArgs.add(GzipUtil.gzip(arg));
            }
            queryByChaincodeRequest.setArgs(zippedArgs);
        } else {
            queryByChaincodeRequest.setArgs(args);
        }

        List<Peer> peers = new ArrayList<>();
        int total = channel.getPeers().size();
        AtomicInteger errorOrFail = new AtomicInteger();
        long st;
        long et;
        while (true) {
            Peer peer = PeerHealthyCache.getHealthyPeer(channel, chaincodeID.getName(), method);
            peers.add(peer);
            st = System.currentTimeMillis();
            Collection<ProposalResponse> queryProposals;
            try {
                queryProposals = channel.queryByChaincode(queryByChaincodeRequest, peers);
            } catch (InvalidArgumentException e) {
                throw e;
            } catch (Exception e) {
                log.error("do query failed on peer {}", peer.getName(), e);
                errorOrFail.getAndIncrement();
                // set time to one day later
                PeerHealthyCache.removeUnhealthyPeer(channel.getName(), chaincodeID.getName(), method, peer);
                if (errorOrFail.get() >= total) {
                    throw new ProposalException("failed on query all peers.", e);
                }
                continue;
            }
            et = System.currentTimeMillis();
            ProposalResponse response = queryProposals.iterator().next();
            if (response.isVerified() && ChaincodeResponse.Status.SUCCESS == response.getStatus()) {
                String payload = response.getProposalResponse().getResponse().getPayload().toStringUtf8();
                log.info("Query payload of {} from peer {} returned message: {}",
                        args == null || args.length == 0 ? null : args[0], response.getPeer().getName(),
                        response.getProposalResponse().getResponse().getMessage());
                PeerHealthyCache.logPeerHealthyData(channel.getName(), chaincodeID.getName(), method, peer, (et - st));
                try {
                	log.debug("Query result {}",payload);
                    Object zippedObj = JsonUtil.toObject(payload, Object.class);
                    return JsonUtil.toJson(unzip(zippedObj));
                } catch (IOException e) {
                    return GzipUtil.unzip(payload);
                }
            } else {
                log.error("Failed query proposal from peer {} through channel {}. status: {}. Messages: {}.",
                        peer.getName(), channel.getName(), response.getStatus(), response.getMessage());
                peers.clear();
                errorOrFail.getAndIncrement();
                // set time to one hour later
                PeerHealthyCache.logPeerHealthyData(channel.getName(), chaincodeID.getName(), method, peer, (3600000 + et - st));
                if (errorOrFail.get() >= total) {
                    throw new ProposalException("failed on query all peers: " + response.getMessage());
                }
            }
        }
    }

    private static Object unzip(Object zippedObj) {
        if (zippedObj instanceof Map) {
            Map<?,?> mapObj = (Map<?, ?>) zippedObj;
            Map<Object, Object> result = new HashMap<>();
            mapObj.forEach((k, v) -> result.put(unzip(k), unzip(v)));
            return result;
        } else if (zippedObj instanceof Collection) {
            Collection<?> collObj = (Collection<?>) zippedObj;
            List<Object> result = new ArrayList<>();
            collObj.forEach(o -> result.add(unzip(o)));
            return result;
        } else if (zippedObj instanceof String) {
            return GzipUtil.unzip((String) zippedObj);
        } else {
            return zippedObj;
        }
    }

    /**
     * 查询交易结果校验码
     *
     * @param user    用户，必须含有完整的Enrollment
     * @param channel 通道，与部署时通道一致
     * @param txID    交易ID
     * @return FabricTransaction.TxValidationCode
     */
    public static FabricTransaction.TxValidationCode queryTxValidationCode(User user, Channel channel, String txID) {
        if (txID == null) return null;
        txID = txID.trim();
        if (txID.isEmpty()) return null;

        try {
            TransactionInfo transactionInfo = channel.queryTransactionByID(txID, user);
            return transactionInfo == null ? null : transactionInfo.getValidationCode();
        } catch (ProposalException | InvalidArgumentException e) {
            return null;
        }
    }
}
