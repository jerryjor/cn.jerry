package cn.jerry.fabric.api.fabric.service;

import cn.jerry.fabric.api.fabric.conf.ChannelConfig;
import cn.jerry.fabric.api.fabric.conf.OrdererConfig;
import cn.jerry.fabric.api.fabric.conf.OrgConfig;
import cn.jerry.fabric.api.fabric.conf.TLSConfig;
import cn.jerry.fabric.api.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.fabric.api.fabric.model.SampleChaincode;
import cn.jerry.fabric.api.fabric.model.SampleOrg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.hyperledger.fabric.sdk.Channel.PeerOptions.createPeerOptions;

@Service
public class ChannelService {
    private static Logger log = LogManager.getLogger();

    private static final ChannelConfig CHANNEL_CONF = ChannelConfig.getInstance();
    private static final TLSConfig TLS_CONF = TLSConfig.getInstance();
    private static final OrgConfig ORG_CONF = OrgConfig.getInstance();
    private static final OrdererConfig ORDERER_CONF = OrdererConfig.getInstance();

    private static ChannelService instance = new ChannelService();
    private static final Lock LOCK = new ReentrantLock();

    private ChannelService() {
    }

    public static ChannelService getInstance() {
        return instance;
    }

    /**
     * 根据链码名字查找，按约定规则查找对应的通道名，如果存在则返回公共通道名
     *
     * @param chaincode 链码信息
     * @return 通道名称
     */
    public String getChannelName(SampleChaincode chaincode) {
        String privateChannelName = chaincode.getName().replaceAll("[^0-9a-zA-Z]", ".") + ".channel";
        try {
            CHANNEL_CONF.getChannelFile(privateChannelName);
            return privateChannelName;
        } catch (FileNotFoundException e) {
            return CHANNEL_CONF.getPublicChannelName();
        }
    }

    /**
     * 构建链码匹配的通道，如果通道已经构建过，返回缓存的通道
     *
     * @param org       机构，用于构建HFClient
     * @param chaincode 链码信息
     * @return 通道，已初始化完毕
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     * @throws IOException                    读取必要的文件失败，可能缺少配置文件
     * @throws InvalidArgumentException       构建通道时节点等信息有误
     * @throws TransactionException           初始化通道失败
     */
    public Channel getChannel(SampleOrg org, SampleChaincode chaincode)
            throws InitializeCryptoSuiteException, IOException, InvalidArgumentException, TransactionException {
        String channelName = getChannelName(chaincode);
        return getChannel(org, channelName);
    }

    /**
     * 构建链码匹配的通道，如果通道已经构建过，返回缓存的通道
     * 每个通道只能有一个实例
     *
     * @param org         机构，用于构建HFClient
     * @param channelName 通道名
     * @return 通道，已初始化完毕
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     * @throws IOException                    读取必要的文件失败，可能缺少配置文件
     * @throws InvalidArgumentException       构建通道时节点等信息有误
     * @throws TransactionException           初始化通道失败
     */
    public Channel getChannel(SampleOrg org, String channelName)
            throws InitializeCryptoSuiteException, IOException, InvalidArgumentException, TransactionException {
        Channel channel = CHANNEL_CONF.getRegisteredChannel(channelName);
        // 如果没有，尝试重新构建channel
        if (channel == null || channel.isShutdown()) {
            LOCK.lock();
            try {
                // double check
                channel = CHANNEL_CONF.getRegisteredChannel(channelName);
                if (channel == null || channel.isShutdown()) {
                    // channel在列表中不存在，尝试初始化channel，需要更换身份为peerAdmin
                    HFClient client = ClientTool.getUserHFClient(org.getPeerAdmin());
                    try {
                        channel = reconstructChannel(client, channelName);
                    } catch (AssertionError e) {
                        channel = constructChannel(client, channelName);
                    }
                    CHANNEL_CONF.registerChannel(channel);
                    log.info("Finished initialization channel {}", channelName);

                    channel.registerBlockListener(blockEvent -> {
                        log.info("channel.blockListener, block number: {}", blockEvent.getBlockNumber());
                    });
//                  channel.registerChaincodeEventListener(Pattern.compile(".*"),
//                           Pattern.compile(Pattern.quote(EXPECTED_EVENT_NAME)),
//                          (handle, blockEvent, ccEvent) -> {
//                              log.info("channel.ccEventListener, cc id: {}, event name: {}, event data: {}, tx id: {}",
//                                      ccEvent.getChaincodeId(), ccEvent.getEventName(), ccEvent.getPayload(), ccEvent.getTxId());
//                          });
                }
            } finally {
                LOCK.unlock();
            }
        }

        return channel;
    }

    /**
     * 通道已经初始化过，重新构建
     *
     * @param client      HFClient
     * @param channelName 通道名
     * @return 通道，已初始化完毕
     * @throws IOException              读取必要的文件失败，可能缺少配置文件
     * @throws InvalidArgumentException 构建通道时节点等信息有误
     * @throws TransactionException     初始化通道失败
     */
    private Channel reconstructChannel(HFClient client, String channelName)
            throws IOException, InvalidArgumentException, TransactionException {
        log.info("Reconstructing channel {}", channelName);
        List<Orderer> ordererList = getOrdererList(client);
        if (ordererList.isEmpty()) {
            throw new InvalidArgumentException("Orderer services are not reachable.");
        }

        List<Peer> peers = getPeersByChannel(client, channelName);
        if (peers.isEmpty()) {
            throw new AssertionError(String.format("Peers does not appear to belong to channel %s", channelName));
        }

        Channel newChannel = client.newChannel(channelName);

        log.debug("adding order to channel {}", channelName);
        ordererList.forEach(orderer -> {
            try {
                newChannel.addOrderer(orderer);
                log.info("Orderer {} joined channel {}", orderer.getName(), channelName);
            } catch (InvalidArgumentException e) {
                log.error("Orderer {} failed to join channel {}", orderer.getName(), channelName, e);
            }
        });

        // Default is all roles.
        //TODO This could be configured in properties file or DB in future
        Channel.PeerOptions peerOptions = createPeerOptions().setPeerRoles(EnumSet.of(Peer.PeerRole.ENDORSING_PEER,
                Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.CHAINCODE_QUERY, Peer.PeerRole.EVENT_SOURCE));
        peers.forEach(peer -> {
            try {
                newChannel.addPeer(peer, peerOptions);
                log.info("Peer {} added to channel {}", peer.getName(), channelName);
            } catch (InvalidArgumentException e) {
                log.info("Peer {} failed to add to channel {}", peer.getName(), channelName, e);
            }
        });
        // 1.3 之后废弃 EventHub

        newChannel.initialize();
        return newChannel;
    }

    /**
     * 首次构建通道
     * 需要通道相应的二进制文件*.tx，该文件可通过configtxgen工具提前生成，详细介绍如下：
     * Channel configuration files are needed when creating a new channel.
     * This is created with the Hyperledger Fabric `configtxgen` tool.
     * command: configtxgen -outputCreateChannelTx foo.tx -profile TwoOrgsChannel_v13 -channelID foo
     * This should produce the following files in the same directory: foo.tx
     * <p>
     * This must be run after `cryptogen` and the directory you're running in **must** have a generated `org` directory.
     * If `build/bin/configtxgen` tool is not present  run `make configtxgen`
     *
     * @param client      HFClient
     * @param channelName 通道名
     * @return 通道，已初始化完毕
     * @throws IOException              读取必要的文件失败，可能缺少配置文件
     * @throws InvalidArgumentException 构建通道时节点等信息有误
     * @throws TransactionException     初始化通道失败
     */
    private Channel constructChannel(HFClient client, String channelName)
            throws IOException, InvalidArgumentException, TransactionException {
        log.info("Constructing channel {}", channelName);
        List<Orderer> ordererList = getOrdererList(client);
        if (ordererList.isEmpty()) {
            throw new AssertionError("Orderer services are not reachable.");
        }

        List<Peer> peers = new ArrayList<>();
        File txFile = CHANNEL_CONF.getChannelFile(channelName);
        ChannelConfiguration channelConfiguration = new ChannelConfiguration(txFile);
        // Create channel that has only one signer that is this orgs peer admin.
        // If channel creation policy needed more signature they would need to be added too.
        byte[][] signatures = new byte[ORG_CONF.getSampleOrgs().size()][];
        int i = 0;
        for (SampleOrg org : ORG_CONF.getSampleOrgs()) {
            peers.addAll(getPeersByOrg(client, org));
            signatures[i] = client.getChannelConfigurationSignature(channelConfiguration, org.getPeerAdmin());
            i++;
        }
        //Just pick the first orderer in the list to create the channel.
        Orderer anOrderer = ordererList.get(0);
        ordererList.remove(0);
        Channel newChannel = client.newChannel(channelName, anOrderer, channelConfiguration, signatures);

        log.debug("adding order to channel {}", channelName);
        ordererList.forEach(orderer -> {
            try {
                newChannel.addOrderer(orderer);
                log.info("Orderer {} joined channel {}", orderer.getName(), channelName);
            } catch (InvalidArgumentException e) {
                log.error("Orderer {} failed to join channel {}", orderer.getName(), channelName, e);
            }
        });

        // Default is all roles.
        //TODO This could be configured in properties file or DB in future
        Channel.PeerOptions peerOptions = createPeerOptions().setPeerRoles(EnumSet.of(Peer.PeerRole.ENDORSING_PEER,
                Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.CHAINCODE_QUERY, Peer.PeerRole.EVENT_SOURCE));
        peers.forEach(peer -> {
            try {
                newChannel.joinPeer(peer, peerOptions);
                log.info("Peer {} joined channel {}", peer.getName(), channelName);
            } catch (ProposalException e) {
                log.info("Peer {} failed to join channel {}", peer.getName(), channelName, e);
            }
        });
        // 1.3 之后废弃 EventHub

        newChannel.initialize();
        return newChannel;
    }

    private List<Orderer> getOrdererList(HFClient client) throws FileNotFoundException {
        List<Orderer> ordererList = new ArrayList<>();
        List<FileNotFoundException> exList = new ArrayList<>();
        ORDERER_CONF.getOrdererLocations().forEach((name, url) -> {
            try {
                Properties ordererProperties = TLS_CONF.getTlsProperties(name);
                // setting keepAlive to avoid timeouts on inactive http2 connections.
                // Under 5 minutes would require changes to server side to accept faster ping rates.
                ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime",
                        new Object[]{CHANNEL_CONF.getGrpcKeepAliveTime(), TimeUnit.MILLISECONDS});
                ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout",
                        new Object[]{CHANNEL_CONF.getGrpcKeepAliveTimeout(), TimeUnit.MILLISECONDS});
                ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveWithoutCalls", new Object[]{true});
                ordererList.add(client.newOrderer(name, url, ordererProperties));
            } catch (FileNotFoundException e) {
                log.error("create orderer {} failed because of no TLS_CONF properties file.", name, e);
                if (exList.isEmpty()) exList.add(e);
            } catch (InvalidArgumentException e) {
                log.error("create orderer {} failed.", name, e);
            }
        });
        if (!exList.isEmpty()) {
            throw exList.get(0);
        }
        return ordererList;
    }

    private List<Peer> getPeersByChannel(HFClient client, String channelName) throws FileNotFoundException {
        List<Peer> peers = new ArrayList<>();
        List<FileNotFoundException> exList = new ArrayList<>();
        ORG_CONF.getSampleOrgs().forEach(org -> org.getPeerNames().forEach(pn -> {
            String peerLocation = org.getPeerLocation(pn);
            try {
                Peer peer = client.newPeer(pn, peerLocation, TLS_CONF.getTlsProperties(pn));
                // Query the actual peer for which channels it belongs to and check it belongs to this channel
                Set<String> channels = client.queryChannels(peer);
                if (channels != null && channels.contains(channelName)) {
                    peers.add(peer);
                }
            } catch (FileNotFoundException e) {
                log.error("create peer {} failed because of no TLS_CONF properties file.", pn, e);
                if (exList.isEmpty()) exList.add(e);
            } catch (InvalidArgumentException e) {
                log.error("create peer {} failed", pn, e);
            } catch (ProposalException e) {
                log.error("failed to query channels {} on peer {}", channelName, pn, e);
            }
        }));
        if (!exList.isEmpty()) {
            throw exList.get(0);
        }
        return peers;
    }

    private List<Peer> getPeersByOrg(HFClient client, SampleOrg org) throws FileNotFoundException {
        List<Peer> peers = new ArrayList<>();
        List<FileNotFoundException> exList = new ArrayList<>();
        org.getPeerNames().forEach(pn -> {
            String peerLocation = org.getPeerLocation(pn);
            try {
                Peer peer = client.newPeer(pn, peerLocation, TLS_CONF.getTlsProperties(pn));
                peers.add(peer);
            } catch (FileNotFoundException e) {
                log.error("create peer {} failed because of no TLS_CONF properties file.", pn, e);
                if (exList.isEmpty()) exList.add(e);
            } catch (InvalidArgumentException e) {
                log.error("create peer {} failed", pn, e);
            }
        });
        if (!exList.isEmpty()) {
            throw exList.get(0);
        }
        return peers;
    }
}
