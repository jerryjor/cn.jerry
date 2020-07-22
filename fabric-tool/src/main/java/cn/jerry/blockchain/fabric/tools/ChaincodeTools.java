package cn.jerry.blockchain.fabric.tools;

import cn.jerry.blockchain.fabric.conf.ChaincodeConfig;
import cn.jerry.blockchain.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.blockchain.fabric.model.FabricChaincode;
import cn.jerry.blockchain.fabric.model.FabricOrg;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ChaincodeTools {
    private static final Logger log = LoggerFactory.getLogger(ChaincodeTools.class);

    private static final ChaincodeConfig CC_CONF = ChaincodeConfig.getInstance();

    private ChaincodeTools() {
        super();
    }

    public static Map<ChaincodeID, List<String>> queryInstalledChaincodes(Channel channel) {
        Map<String, ChaincodeID> temp1 = new HashMap<>();
        Map<String, List<String>> temp2 = new HashMap<>();
        for (Peer peer : channel.getPeers()) {
            try {
                List<Query.ChaincodeInfo> ccInfoList = channel.queryInstantiatedChaincodes(peer);
                if (ccInfoList != null) {
                    ccInfoList.forEach(ccInfo -> {
                        String key = ccInfo.getName() + "|" + ccInfo.getVersion();
                        temp1.computeIfAbsent(key, k -> ChaincodeID.newBuilder().setName(ccInfo.getName()).setVersion(ccInfo.getVersion()).build());
                        List<String> peerList = temp2.computeIfAbsent(key, k -> new ArrayList<>());
                        peerList.add(peer.getUrl());
                    });
                }
            } catch (InvalidArgumentException | ProposalException e) {
                log.error("Failed to query instantiated chaincodes on peer {}", peer.getName(), e);
            }
        }
        Map<ChaincodeID, List<String>> results = new HashMap<>();
        temp1.forEach((key, cc) -> results.put(cc, temp2.get(key)));
        return results;
    }

    /**
     * 部署链码
     *
     * @param org       机构信息
     * @param chaincode 链码信息
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     * @throws IOException                    读取必要的文件失败，可能缺少配置文件
     * @throws InvalidArgumentException       构建通道时节点等信息有误
     * @throws TransactionException           初始化通道失败
     * @throws ProposalException              请求安装失败
     */
    public static void install(FabricOrg org, FabricChaincode chaincode)
            throws InitializeCryptoSuiteException, ProposalException, TransactionException, IOException, InvalidArgumentException {
        log.info("Installing chaincode:{}", chaincode);

        HFClient client = ClientTool.getUserHFClient(org.getPeerAdmin());
        Channel channel = ChannelTools.getChannel(org, chaincode.getId());
        int total = 0;
        int successful = 0;
        int failed = 0;
        String failMessage = null;
        for (Peer peer : channel.getPeers()) {
            total++;
            try {
                installByPeer(client, chaincode, peer);
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

    private static void installByPeer(HFClient adminClient, FabricChaincode chaincode, Peer peer)
            throws ProposalException, InvalidArgumentException {
        if (isInstalled(adminClient, chaincode, peer)) {
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
        //installProposalRequest.setChaincodeMetaInfLocation(Paths.get(env.getBasePath(), "meta-infs/end2endit").toFile())
        installProposalRequest.setProposalWaitTime(CC_CONF.getDeployTimeout());
        log.info("Sending chaincode[{}] install proposal to peer {}", chaincode.getName(), peer.getName());

        Collection<ProposalResponse> responses = adminClient.sendInstallProposal(installProposalRequest, peers);
        ProposalResponse response = responses.iterator().next();
        if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
            log.info("Chaincode[{}] install proposal response succeed from peer {}. Txid: {}", chaincode.getName(),
                    peer.getName(), response.getTransactionID());
        } else {
            log.info("Chaincode[{}] install proposal response failed from peer {}. Txid: {}. Message: {}",
                    chaincode.getName(), peer.getName(), response.getTransactionID(), response.getMessage());
            throw new ProposalException(response.getMessage());
        }
    }

    private static boolean isInstalled(HFClient adminClient, FabricChaincode chaincode, Peer peer)
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
     * @param org       机构信息
     * @param chaincode 链码信息
     * @param initData  需要初始化的数据，非必须
     * @return 交易ID
     * @throws InitializeCryptoSuiteException           初始化加密算法实现类失败，无法创建HFClient
     * @throws IOException                              读取必要的文件失败，可能缺少配置文件
     * @throws TransactionException                     初始化通道失败
     * @throws ChaincodeEndorsementPolicyParseException 无法找到该链码的背书策略
     * @throws InvalidArgumentException                 构建通道时节点等信息有误/请求启动服务或初始化数据的参数有误
     * @throws ProposalException                        请求启动服务或初始化数据失败
     * @throws ExecutionException                       获取初始化数据的交易结果失败
     */
    public static String instantiate(FabricOrg org, FabricChaincode chaincode, Map<String, String> initData)
            throws InitializeCryptoSuiteException, ChaincodeEndorsementPolicyParseException, InvalidArgumentException,
            ProposalException, ExecutionException, IOException, TransactionException {
        log.info("Instantiating chaincode:{}", chaincode);

        Channel channel = ChannelTools.getChannel(org, chaincode.getId());
        if (isInstantiated(channel, chaincode, channel.getPeers().iterator().next())) {
            log.info("Chaincode[{}] is already instantiated on channel {}", chaincode.getName(), channel.getName());
            return null;
        }

        HFClient client = ClientTool.getUserHFClient(org.getPeerAdmin());
        InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
        instantiateProposalRequest.setChaincodeID(chaincode.getId());
        try {
            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromStream(CC_CONF.getDefaultEndorsementPolicy());
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

        int total = 0;
        int successful = 0;
        int failed = 0;
        String txId = null;
        String failMessage = null;
        ArrayList<ProposalResponse> successfulProposals = new ArrayList<>();
        Collection<ProposalResponse> responses = channel.sendInstantiationProposal(instantiateProposalRequest,
                channel.getPeers());
        for (ProposalResponse response : responses) {
            total++;
            if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
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
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            log.warn("get result of sending transaction timeout. txId: {}", txId);
        }
        return txId;
    }

    private static boolean isInstantiated(Channel channel, FabricChaincode chaincode, Peer peer) throws InvalidArgumentException,
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
     * @param org       机构信息
     * @param chaincode 链码信息
     * @return 交易ID
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     * @throws IOException                    读取必要的文件失败，可能缺少配置文件
     * @throws TransactionException           初始化通道失败
     * @throws InvalidArgumentException       构建通道时节点等信息有误/请求启动服务或初始化数据的参数有误
     * @throws ProposalException              请求启动服务或初始化数据失败
     * @throws ExecutionException             获取初始化数据的交易结果失败
     */
    public static String upgrade(FabricOrg org, FabricChaincode chaincode)
            throws InitializeCryptoSuiteException, InvalidArgumentException, ProposalException, ExecutionException, IOException, TransactionException {
        log.info("Upgrading chaincode:{}", chaincode);

        HFClient client = ClientTool.getUserHFClient(org.getPeerAdmin());
        UpgradeProposalRequest upgradeProposalRequest = client.newUpgradeProposalRequest();
        upgradeProposalRequest.setChaincodeID(chaincode.getId());
        upgradeProposalRequest.setChaincodeVersion(chaincode.getVersion());
        upgradeProposalRequest.setProposalWaitTime(CC_CONF.getProposalTimeout());
        upgradeProposalRequest.setFcn("init");
        Map<String, byte[]> txMap = new HashMap<>();
        txMap.put("HyperLedgerFabric", "UpgradeProposalRequest:JavaSDK".getBytes(UTF_8));
        txMap.put("method", "UpgradeProposalRequest".getBytes(UTF_8));
        upgradeProposalRequest.setTransientMap(txMap);
        log.info("Sending chaincode[{}] upgrade proposal to peers", chaincode.getName());

        Channel channel = ChannelTools.getChannel(org, chaincode.getId());
        int total = 0;
        int successful = 0;
        int failed = 0;
        String txId = null;
        String failMessage = null;
        ArrayList<ProposalResponse> successfulProposals = new ArrayList<>();
        Collection<ProposalResponse> responses = channel.sendUpgradeProposal(upgradeProposalRequest, channel.getPeers());
        for (ProposalResponse response : responses) {
            total++;
            if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
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
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            log.warn("get result of sending transaction timeout. txId: {}", txId);
        }
        return txId;
    }

}
