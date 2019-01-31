package cn.jerry.fabric.api.fabric.service;

import cn.jerry.fabric.api.fabric.conf.ChaincodeConfig;
import cn.jerry.fabric.api.fabric.conf.OrgConfig;
import cn.jerry.fabric.api.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.fabric.api.fabric.model.SampleChaincode;
import cn.jerry.fabric.api.fabric.model.SampleOrg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class ChaincodeService {
    private static Logger log = LogManager.getLogger();

    private static final ChaincodeConfig CC_CONF = ChaincodeConfig.getInstance();
    private static final OrgConfig ORG_CONF = OrgConfig.getInstance();

    /**
     * 部署链码
     *
     * @param org       机构，用于构建HFClient
     * @param channel   通道
     * @param chaincode 链码信息
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     * @throws ProposalException              请求安装失败
     */
    public void install(SampleOrg org, Channel channel, SampleChaincode chaincode)
            throws InitializeCryptoSuiteException, ProposalException {
        log.info("Installing chaincode:{}", chaincode);

        HFClient client = ClientTool.getUserHFClient(org.getPeerAdmin());
        int total = 0, successful = 0, failed = 0;
        String failMessage = null;
        for (Peer peer : channel.getPeers()) {
            total++;
            try {
                installByPeer(client, channel, chaincode, peer);
                successful++;
            } catch (Exception e) {
                failMessage = e.getMessage();
                failed++;
            }
        }
        log.info("Received {} chaincode[{}] install proposal responses. Successful: {}. Failed: {}", total,
                chaincode.getName(), successful, failed);

        if (failed > 0) {
            log.error("Not enough endorsers for chaincode[{}] install, {} failed with: {}", chaincode.getName(),
                    failed, failMessage);
            throw new ProposalException("One or more install proposal response failed: " + failMessage);
        }
    }

    private void installByPeer(HFClient adminClient, Channel channel, SampleChaincode chaincode, Peer peer)
            throws ProposalException, InvalidArgumentException {
        if (isInstalled(adminClient, channel, chaincode, peer)) {
            log.info("Chaincode[{}] is already installed on peer {}.", chaincode.getName(), peer.getName());
            return;
        }
        ArrayList<Peer> peers = new ArrayList<>();
        peers.add(peer);

        InstallProposalRequest installProposalRequest = adminClient.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincode.getId());
        installProposalRequest.setChaincodeName(chaincode.getName());
        installProposalRequest.setChaincodeVersion(chaincode.getVersion());
        installProposalRequest.setChaincodeLanguage(chaincode.getLang());
        if (TransactionRequest.Type.GO_LANG == chaincode.getLang()) {
            installProposalRequest.setChaincodePath(chaincode.getInstallPath());
        }
        installProposalRequest.setChaincodeInputStream(new ByteArrayInputStream(chaincode.getSourceBytes()));
        // This sets an index on the variable a in the chaincode
        // see http://hyperledger-fabric.readthedocs.io/en/master/couchdb_as_state_database.html#using-couchdb-from-chaincode
        // The file IndexA.json as part of the META-INF will be packaged with the source to create the index.
        //installProposalRequest.setChaincodeMetaInfLocation(Paths.get(env.getBasePath(), "meta-infs/end2endit").toFile());
        installProposalRequest.setProposalWaitTime(CC_CONF.getDeployTimeout());
        log.info("Sending chaincode[{}] install proposal to peer {}", chaincode.getName(), peer.getName());

        Collection<ProposalResponse> responses = adminClient.sendInstallProposal(installProposalRequest, peers);
        ProposalResponse response = responses.iterator().next();
        if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
            log.info("Chaincode[{}] install proposal response succeed from peer {}. Txid: {}", chaincode.getName(),
                    peer.getName(), response.getTransactionID());
        } else {
            log.info("Chaincode[{}] install proposal response failed from peer {}. Txid: {}. Message: {}",
                    chaincode.getName(), peer.getName(), response.getTransactionID(), response.getMessage());
            throw new ProposalException(response.getMessage());
        }
    }

    private boolean isInstalled(HFClient adminClient, Channel channel, SampleChaincode chaincode, Peer peer)
            throws InvalidArgumentException, ProposalException {
        log.info("Checking installed chaincode: {}, on peer: {}", chaincode, peer.getName());
        List<Query.ChaincodeInfo> ccInfoList = adminClient.queryInstalledChaincodes(peer);
        log.info("Installed chaincodes.size: {}", ccInfoList.size());

        for (Query.ChaincodeInfo ccInfo : ccInfoList) {
            if (chaincode.getName().equals(ccInfo.getName()) && chaincode.getVersion().equals(ccInfo.getVersion())) {
                if (TransactionRequest.Type.GO_LANG == chaincode.getLang()) {
                    return chaincode.getInstallPath().equals(ccInfo.getPath());
                } else {
                    return ccInfo.getPath() == null || ccInfo.getPath().isEmpty();
                }
            }
        }

        return false;
    }

    /**
     * 启动链码服务
     *
     * @param org       机构，用于构建HFClient
     * @param channel   通道，与部署时通道一致
     * @param chaincode 链码信息
     * @param initData  需要初始化的数据，非必须
     * @return 交易ID
     * @throws InitializeCryptoSuiteException           初始化加密算法实现类失败，无法创建HFClient
     * @throws ChaincodeEndorsementPolicyParseException 无法找到该链码的背书策略
     * @throws InvalidArgumentException                 请求启动服务或初始化数据的参数有误
     * @throws ProposalException                        请求启动服务或初始化数据失败
     * @throws ExecutionException                       获取初始化数据的交易结果失败
     */
    public String instantiate(SampleOrg org, Channel channel, SampleChaincode chaincode, Map<String, String> initData)
            throws InitializeCryptoSuiteException, ChaincodeEndorsementPolicyParseException, InvalidArgumentException, ProposalException, ExecutionException {
        log.info("Instantiating chaincode:{}", chaincode);

        if (isInstantiated(channel, chaincode, channel.getPeers().iterator().next())) {
            log.info("Chaincode[{}] is already instantiated on channel {}", chaincode.getName(), channel.getName());
            return null;
        }

        HFClient client = ClientTool.getUserHFClient(org.getPeerAdmin());
        InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
        instantiateProposalRequest.setChaincodeID(chaincode.getId());
        // instantiateProposalRequest.setChaincodeLanguage(ccDef.getLang());
        try {
            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromYamlFile(CC_CONF.getDefaultEndorsementPolicy());
            instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
        } catch (IOException e) {
            throw new ChaincodeEndorsementPolicyParseException(e.getMessage());
        }
        instantiateProposalRequest.setFcn("init");
        instantiateProposalRequest.setProposalWaitTime(CC_CONF.getProposalTimeout());
        String[] args;
        if (initData == null || initData.isEmpty()) {
            args = new String[]{};
        } else {
            args = new String[initData.size() * 2];
            int index = 0;
            for (Map.Entry<String, String> entry : initData.entrySet()) {
                args[index * 2] = entry.getKey();
                args[index * 2 + 1] = entry.getValue();
                index++;
            }
        }
        instantiateProposalRequest.setArgs(args);
        Map<String, byte[]> txMap = new HashMap<>();
        txMap.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        txMap.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        instantiateProposalRequest.setTransientMap(txMap);
        log.info("Sending chaincode[{}] instantiate proposal to peers with arguments: {}", chaincode.getName(), initData);

        int total = 0, successful = 0, failed = 0;
        String txId = null, failMessage = null;
        ArrayList<ProposalResponse> successfulProposals = new ArrayList<>();
        Collection<ProposalResponse> responses = channel.sendInstantiationProposal(instantiateProposalRequest,
                channel.getPeers());
        for (ProposalResponse response : responses) {
            total++;
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                txId = response.getTransactionID();
                successfulProposals.add(response);
                successful++;
                log.info("Chaincode[{}] instantiate proposal response succeed from peer {}. Txid: {}",
                        chaincode.getName(), response.getPeer().getName(), response.getTransactionID());
            } else {
                failMessage = response.getMessage();
                failed++;
                log.info("Chaincode[{}] instantiate proposal response failed from peer {}. Txid: {}. Message: {}",
                        chaincode.getName(), response.getPeer().getName(), response.getTransactionID(), failMessage);
            }
        }
        log.info("Received {} chaincode[{}] instantiate proposal responses. Successful: {}. Failed: {}", total,
                chaincode.getName(), successful, failed);

        if (failed > 0) {
            log.error("Not enough endorsers for chaincode[{}] instantiate, {} failed with {}.", chaincode.getName(),
                    failed, failMessage);
            throw new ProposalException("One or more instantiate proposal response failed: " + failMessage);
        }

        log.info("Sending chaincode[{}] instantiate({}) to orderer. txId: {}", chaincode.getName(), initData, txId);
        try {
            channel.sendTransaction(successfulProposals,
                    Channel.TransactionOptions.createTransactionOptions().userContext(client.getUserContext())
            ).get(CC_CONF.getInvokeTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.warn("get result of sending transaction is interrupted. txId: {}", txId);
        } catch (TimeoutException e) {
            log.warn("get result of sending transaction timeout. txId: {}", txId);
        }
        return txId;
    }

    private boolean isInstantiated(Channel channel, SampleChaincode chaincode, Peer peer) throws InvalidArgumentException,
            ProposalException {
        log.info("Checking instantiated chaincode: {}, on peer: {}", chaincode, peer.getName());
        List<Query.ChaincodeInfo> ccInfoList = channel.queryInstantiatedChaincodes(peer);
        log.info("Instantiated Chaincodes.size: {}", ccInfoList.size());

        for (Query.ChaincodeInfo ccInfo : ccInfoList) {
            if (chaincode.getName().equals(ccInfo.getName()) && chaincode.getVersion().equals(ccInfo.getVersion())) {
                if (TransactionRequest.Type.GO_LANG == chaincode.getLang()) {
                    return chaincode.getInstallPath().equals(ccInfo.getPath());
                } else {
                    return ccInfo.getPath() == null || ccInfo.getPath().isEmpty();
                }
            }
        }

        return false;
    }

    /**
     * 升级链码，新的版本号必须高于目前版本
     * 链码升级后，旧版本的链码服务依然运行
     * 后续交易或查询如果不提供链码版本参数，默认为最高链码版本
     *
     * @param org       机构，用于构建HFClient
     * @param channel   通道，与部署时通道一致
     * @param chaincode 链码信息
     * @return 交易ID
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     * @throws InvalidArgumentException       请求启动服务或初始化数据的参数有误
     * @throws ProposalException              请求启动服务或初始化数据失败
     * @throws ExecutionException             获取初始化数据的交易结果失败
     */
    public String upgrade(SampleOrg org, Channel channel, SampleChaincode chaincode)
            throws InitializeCryptoSuiteException, InvalidArgumentException, ProposalException, ExecutionException {
        log.info("Upgrading chaincode:{}", chaincode);

        HFClient client = ClientTool.getUserHFClient(org.getPeerAdmin());
        UpgradeProposalRequest upgradeProposalRequest = client.newUpgradeProposalRequest();
        upgradeProposalRequest.setChaincodeID(chaincode.getId());
        upgradeProposalRequest.setChaincodeVersion(chaincode.getVersion());
        upgradeProposalRequest.setProposalWaitTime(CC_CONF.getProposalTimeout());
        upgradeProposalRequest.setFcn("init");
        upgradeProposalRequest.setArgs(new String[]{});
        Map<String, byte[]> txMap = new HashMap<>();
        txMap.put("HyperLedgerFabric", "UpgradeProposalRequest:JavaSDK".getBytes(UTF_8));
        txMap.put("method", "UpgradeProposalRequest".getBytes(UTF_8));
        upgradeProposalRequest.setTransientMap(txMap);
        log.info("Sending chaincode[{}] upgrade proposal to peers", chaincode.getName());

        int total = 0, successful = 0, failed = 0;
        String txId = null, failMessage = null;
        ArrayList<ProposalResponse> successfulProposals = new ArrayList<>();
        Collection<ProposalResponse> responses = channel.sendUpgradeProposal(upgradeProposalRequest, channel.getPeers());
        for (ProposalResponse response : responses) {
            total++;
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                txId = response.getTransactionID();
                successfulProposals.add(response);
                successful++;
                log.info("Chaincode[{}] upgrade proposal response succeed from peer {}. Txid: {}", chaincode.getName(),
                        response.getPeer().getName(), response.getTransactionID());
            } else {
                failMessage = response.getMessage();
                failed++;
                log.info("Chaincode[{}] upgrade proposal response failed from peer {}. Txid: {}. Message: {}",
                        chaincode.getName(), response.getPeer().getName(), response.getTransactionID(), failMessage);
            }
        }
        log.info("Received {} chaincode[{}] upgrade proposal responses. Successful: {}. Failed: {}", total,
                chaincode.getName(), successful, failed);

        if (failed > 0) {
            log.error("Not enough endorsers for chaincode[{}] upgrade, {} failed with {}.", chaincode.getName(),
                    failed, failMessage);
            throw new ProposalException("One or more upgrade proposal response failed: " + failMessage);
        }

        log.info("Sending chaincode[{}] upgrade to orderer. txId: {}", chaincode.getName(), txId);
        try {
            channel.sendTransaction(successfulProposals,
                    Channel.TransactionOptions.createTransactionOptions().userContext(client.getUserContext())
            ).get(CC_CONF.getInvokeTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.warn("get result of sending transaction is interrupted. txId: {}", txId);
        } catch (TimeoutException e) {
            log.warn("get result of sending transaction timeout. txId: {}", txId);
        }
        return txId;
    }

    //TODO stop
}
