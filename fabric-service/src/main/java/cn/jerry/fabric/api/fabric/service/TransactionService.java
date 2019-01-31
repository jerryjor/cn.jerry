package cn.jerry.fabric.api.fabric.service;

import cn.jerry.fabric.api.fabric.cache.PeerHealthyCache;
import cn.jerry.fabric.api.fabric.conf.ChaincodeConfig;
import cn.jerry.fabric.api.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.fabric.api.fabric.model.SampleUser;
import cn.jerry.fabric.api.fabric.util.ThrowableUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.protos.peer.FabricTransaction;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class TransactionService {
    private static Logger log = LogManager.getLogger();

    private static final ChaincodeConfig CC_CONF = ChaincodeConfig.getInstance();
//    private static final String EXPECTED_EVENT_NAME = "event";
//    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
//    private static final String EXPECTED_RESULT_NAME = "result";
//    private static final byte[] EXPECTED_RESULT_DATA = ":)".getBytes(UTF_8);

    /**
     * 执行链码的交易
     *
     * @param user        用户，必须含有完整的Enrollment
     * @param channel     通道，与部署时通道一致
     * @param chaincodeID 链码ID
     * @param method      调用的交易方法
     * @param args        该交易方法所需的参数，有顺序
     * @param sync        是否等待交易结果后同步返回
     * @return 交易id
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     * @throws InvalidArgumentException       参数有误，无法执行请求
     * @throws ProposalException              请求交易失败
     * @throws ExecutionException             获取交易结果失败
     */
    public String doTransaction(SampleUser user, Channel channel, ChaincodeID chaincodeID, String method, String[] args,
            boolean sync) throws InitializeCryptoSuiteException, InvalidArgumentException, ProposalException, ExecutionException {

        HFClient client = ClientTool.getUserHFClient(user);
        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setFcn(method);
        transactionProposalRequest.setProposalWaitTime(CC_CONF.getProposalTimeout());
        transactionProposalRequest.setArgs(args);

        Map<String, byte[]> txMap = new HashMap<>();
        txMap.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        txMap.put("method", "TransactionProposalRequest".getBytes(UTF_8));
//        // These two should be returned, see chaincode why.
//        txMap.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);
//        txMap.put(EXPECTED_RESULT_NAME, EXPECTED_RESULT_DATA);
//        // This should be returned see chaincode why.
//        if (TransactionRequest.Type.GO_LANG == ccDef.getLang()) {
//            txMap.put("rc", ("200").getBytes(UTF_8));
//        }
        transactionProposalRequest.setTransientMap(txMap);
        log.info("sending transactionProposal to all peers with arguments: {}({})", method, Arrays.toString(args));

        int successful = 0, failed = 0;
        String txId = null, failMessage = null;
        final Collection<ProposalResponse> successfulProposals = new LinkedList<>();
        Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(
                transactionProposalRequest, channel.getPeers());
        for (ProposalResponse response : transactionPropResp) {
            txId = response.getTransactionID();
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                successful++;
                successfulProposals.add(response);
                log.info("Successful transaction proposal response Txid: {} from peer {}", txId,
                        response.getPeer().getName());
            } else {
                failed++;
                failMessage = response.getMessage();
                log.error("Fail transaction proposal response Txid: {} from peer {}, {}", txId,
                        response.getPeer().getName(), failMessage);
            }
        }
        log.info("Received {} transaction proposal responses. Successful+verified: {}. Failed: {}",
                transactionPropResp.size(), successful, failed);
        if (successful == 0) {
            log.error("Not enough endorsers for {}({}), {} failed with: {}.", method, Arrays.toString(args),
                    failed, failMessage);
            throw new ProposalException("One or more instantiate proposal response failed: " + failMessage);
        }

        log.info("Sending chaincode transaction({},{}) to orderer. txId: {}", method, Arrays.toString(args), txId);
        CompletableFuture<BlockEvent.TransactionEvent> future = channel.sendTransaction(successfulProposals,
                Channel.TransactionOptions.createTransactionOptions().userContext(client.getUserContext()));
        if (sync) {
            try {
                future.get(CC_CONF.getInvokeTimeout(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException | TimeoutException e) {
                log.warn("get result of sending transaction is interrupted or timeout. txId: {}", txId);
                throw new ExecutionException(txId, e);
            } catch (ExecutionException e) {
                log.info("Sending chaincode proposals to orderer error. txId: {}", txId, e);
                throw new ExecutionException(txId, ThrowableUtil.findRootCause(e));
            }
        }
        return txId;
    }

    /**
     * 执行链码查询
     *
     * @param user        用户，必须含有完整的Enrollment
     * @param channel     通道，与部署时通道一致
     * @param chaincodeID 链码ID
     * @param method      调用的查询方法
     * @param args        该查询方法所需的参数，有顺序
     * @return 查询结果，payload
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     * @throws InvalidArgumentException       参数有误，无法执行请求
     * @throws ProposalException              查询失败
     */
    public String doQuery(SampleUser user, Channel channel, ChaincodeID chaincodeID, String method, String[] args)
            throws InitializeCryptoSuiteException, InvalidArgumentException, ProposalException {
        HFClient client = ClientTool.getUserHFClient(user);
        QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
        Map<String, byte[]> txMap = new HashMap<>();
        txMap.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        txMap.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
        queryByChaincodeRequest.setTransientMap(txMap);
        queryByChaincodeRequest.setChaincodeID(chaincodeID);
        queryByChaincodeRequest.setFcn(method);
        queryByChaincodeRequest.setArgs(args);

        List<Peer> peers = new ArrayList<>();
        int total = channel.getPeers().size(), errorOrFail = 0;
        long st, et;
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
                errorOrFail++;
                // set time to one day later
                PeerHealthyCache.removeUnhealthyPeer(channel.getName(), chaincodeID.getName(), method, peer);
                if (errorOrFail >= total) {
                    throw new ProposalException("failed on query all peers.", e);
                }
                continue;
            }
            et = System.currentTimeMillis();
            ProposalResponse response = queryProposals.iterator().next();
            if (response.isVerified() && ProposalResponse.Status.SUCCESS == response.getStatus()) {
                String payload = response.getProposalResponse().getResponse().getPayload().toStringUtf8();
                log.info("Query payload of {} from peer {} returned message: {}", Arrays.toString(args),
                        response.getPeer().getName(), response.getProposalResponse().getResponse().getMessage());
                PeerHealthyCache.logPeerHealthyData(channel.getName(), chaincodeID.getName(), method, peer, (et - st));
                return payload;
            } else {
                log.error("Failed query proposal from peer {} status: {}. Messages: {}.", peer.getName(),
                        response.getStatus(), response.getMessage());
                peers.clear();
                errorOrFail++;
                // set time to one hour later
                PeerHealthyCache.logPeerHealthyData(channel.getName(), chaincodeID.getName(), method, peer, (3600000 + et - st));
                if (errorOrFail >= total) {
                    throw new ProposalException("failed on query all peers: " + response.getMessage());
                }
            }
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
    public FabricTransaction.TxValidationCode queryTxValidationCode(SampleUser user, Channel channel, String txID) {
        if (txID == null || (txID = txID.trim()).isEmpty()) return null;

        try {
            TransactionInfo transactionInfo = channel.queryTransactionByID(txID, user);
            return transactionInfo == null ? null : transactionInfo.getValidationCode();
        } catch (ProposalException | InvalidArgumentException e) {
            return null;
        }
    }
}
