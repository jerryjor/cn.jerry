package cn.jerry.blockchain.fabric.tools;

import cn.jerry.blockchain.fabric.conf.ChannelConfig;
import cn.jerry.blockchain.fabric.conf.OrdererConfig;
import cn.jerry.blockchain.fabric.conf.OrgConfig;
import cn.jerry.blockchain.fabric.conf.TLSConfig;
import cn.jerry.blockchain.fabric.event.BlockCreatedListener;
import cn.jerry.blockchain.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.blockchain.fabric.model.FabricOrg;
import cn.jerry.blockchain.util.ThrowableUtil;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hyperledger.fabric.sdk.Channel.PeerOptions.createPeerOptions;

public class ChannelTools {
    private static final Logger log = LoggerFactory.getLogger(ChannelTools.class);

    private static final ChannelConfig CHANNEL_CONF = ChannelConfig.getInstance();
    private static final TLSConfig TLS_CONF = TLSConfig.getInstance();
    private static final OrgConfig ORG_CONF = OrgConfig.getInstance();
    private static final OrdererConfig ORDERER_CONF = OrdererConfig.getInstance();

    private ChannelTools() {
        super();
    }

    /**
     * 根据链码名字查找，按约定规则查找对应的通道名，如果存在则返回公共通道名
     *
     * @param ccID 链码ID
     * @return 通道名称
     */
    public static String getChannelName(ChaincodeID ccID) {
        String privateChannelName = ccID.getName().replaceAll("[^0-9a-zA-Z]", ".") + ".channel";
        byte[] file = CHANNEL_CONF.getChannelFile(privateChannelName);
        return file.length == 0 ? CHANNEL_CONF.getPublicChannelName() : privateChannelName;
    }

    /**
     * 构建链码匹配的通道，如果通道已经构建过，返回缓存的通道
     *
     * @param org  机构信息
     * @param ccID 链码ID
     * @return 通道，已初始化完毕
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     * @throws InvalidArgumentException       构建通道时节点等信息有误
     * @throws TransactionException           初始化通道失败
     */
    public static Channel getChannel(FabricOrg org, ChaincodeID ccID)
            throws InitializeCryptoSuiteException, InvalidArgumentException, TransactionException {
        String channelName = getChannelName(ccID);
        return getChannel(org, channelName);
    }

    /**
     * 构建链码匹配的通道，如果通道已经构建过，返回缓存的通道
     * 每个通道只能有一个实例
     *
     * @param org         机构信息
     * @param channelName 通道名
     * @return 通道，已初始化完毕
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     * @throws InvalidArgumentException       构建通道时节点等信息有误
     * @throws TransactionException           初始化通道失败
     */
    public static Channel getChannel(FabricOrg org, String channelName)
            throws InitializeCryptoSuiteException, InvalidArgumentException, TransactionException {
        Channel channel = CHANNEL_CONF.getRegisteredChannel(channelName);
        // 如果没有，尝试重新构建channel
        if (channel == null || channel.isShutdown()) {
            synchronized (CHANNEL_CONF) {
                // double check
                channel = CHANNEL_CONF.getRegisteredChannel(channelName);
                if (channel == null || channel.isShutdown()) {
                    try {
                        channel = reconstructChannel(org, channelName);
                    } catch (AssertionError e) {
                        channel = constructChannel(org, channelName);
                    }
                    CHANNEL_CONF.registerChannel(channel);
                    log.info("Finished initialization channel {}", channelName);
                    reregisterListeners(channel);
                }
            }
        }

        return channel;
    }

    /**
     * 通道已经初始化过，重新构建
     *
     * @param org         机构信息
     * @param channelName 通道名
     * @return 通道，已初始化完毕
     * @throws InvalidArgumentException       构建通道时节点等信息有误
     * @throws TransactionException           初始化通道失败
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     */
    private static Channel reconstructChannel(FabricOrg org, String channelName)
            throws InvalidArgumentException, TransactionException, InitializeCryptoSuiteException {
        log.info("Reconstructing channel {}", channelName);
        HFClient client = ClientTool.getUserHFClient(org.getPeerAdmin());
        List<Orderer> ordererList = getOrdererList(client);
        if (ordererList.isEmpty()) {
            throw new InvalidArgumentException("Orderer services are not reachable.");
        }

        Map<String, Map<Peer, EnumSet<Peer.PeerRole>>> map = getPeers(client, false, channelName);
        Map<Peer, EnumSet<Peer.PeerRole>> joinedPeers = map.get("joined");
        Map<Peer, EnumSet<Peer.PeerRole>> freePeers = map.get("free");
//        if (joinedPeers.isEmpty()) {
//            throw new AssertionError(String.format("Peers does not appear to belong to channel %s", channelName));
//        }

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

        joinedPeers.forEach((peer, roles) -> {
            try {
                newChannel.addPeer(peer, createPeerOptions().setPeerRoles(roles));
                log.info("Peer {} added to channel {}", peer.getName(), channelName);
            } catch (InvalidArgumentException e) {
                log.info("Peer {} failed to add to channel {}", peer.getName(), channelName, e);
            }
        });
        if (!freePeers.isEmpty()) {
            freePeers.forEach((peer, roles) -> {
                try {
                    newChannel.joinPeer(peer, createPeerOptions().setPeerRoles(roles));
                    log.info("Peer {} join to channel {}", peer.getName(), channelName);
                } catch (ProposalException e) {
                    log.info("Peer {} failed to join to channel {}", peer.getName(), channelName, e);
                }
            });
        }

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
     * @param org         机构信息
     * @param channelName 通道名
     * @return 通道，已初始化完毕
     * @throws InvalidArgumentException       构建通道时节点等信息有误
     * @throws TransactionException           初始化通道失败
     * @throws InitializeCryptoSuiteException 初始化加密算法实现类失败，无法创建HFClient
     */
    private static Channel constructChannel(FabricOrg org, String channelName)
            throws InvalidArgumentException, TransactionException, InitializeCryptoSuiteException {
        log.info("Constructing channel {}", channelName);
        HFClient client = ClientTool.getUserHFClient(org.getPeerAdmin());
        List<Orderer> ordererList = getOrdererList(client);
        if (ordererList.isEmpty()) {
            throw new AssertionError("Orderer services are not reachable.");
        }

        // 读取当前channel配置的peer列表
        // 从org中查询peer.location属性，构建peer，add到channel
        Map<String, Map<Peer, EnumSet<Peer.PeerRole>>> map = getPeers(client, true, null);
        Map<Peer, EnumSet<Peer.PeerRole>> freePeers = map.get("free");
        byte[] txFile = CHANNEL_CONF.getChannelFile(channelName);
        ChannelConfiguration channelConfiguration = new ChannelConfiguration(txFile);
        // Create channel that has only one signer that is this orgs peer admin.
        // If channel creation policy needed more signature they would need to be added too.
        byte[][] signatures = getAdminSignatures(channelConfiguration);
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

        log.debug("adding peer to channel {}", channelName);
        freePeers.forEach((peer, roles) -> {
            try {
                newChannel.joinPeer(peer, createPeerOptions().setPeerRoles(roles));
                log.info("Peer {} joined channel {}", peer.getName(), channelName);
            } catch (ProposalException e) {
                log.info("Peer {} failed to join channel {}", peer.getName(), channelName, e);
            }
        });

        newChannel.initialize();
        return newChannel;
    }

    private static byte[][] getAdminSignatures(ChannelConfiguration channelConfiguration) {
        List<byte[]> signatures = new ArrayList<>();
        for (FabricOrg org : ORG_CONF.getOrgs()) {
            if (org.getPeerAdmin() != null) {
                try {
                    HFClient client = ClientTool.getUserHFClient(org.getPeerAdmin());
                    signatures.add(client.getChannelConfigurationSignature(channelConfiguration, org.getPeerAdmin()));
                } catch (InvalidArgumentException e) {
                    log.error("Failed to get channel configuration signature.", e);
                } catch (InitializeCryptoSuiteException e) {
                    log.error("Failed to create HFClient.", e);
                }
            } else {
                log.warn("Peer admin is NOT registered on org {}", org.getName());
            }
        }
        return signatures.toArray(new byte[signatures.size()][]);
    }

    private static List<Orderer> getOrdererList(HFClient client) {
        List<Orderer> ordererList = new ArrayList<>();
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
            } catch (InvalidArgumentException e) {
                log.error("create orderer {} failed.", name, e);
            } catch (IOException e) {
                log.error("Failed to create orderer {}, because failed to read cert file.", name, e);
            }
        });
        return ordererList;
    }

    private static Map<String, Map<Peer, EnumSet<Peer.PeerRole>>> getPeers(HFClient client, boolean construct,
            String channelName) {
        Map<String, Map<Peer, EnumSet<Peer.PeerRole>>> map = new HashMap<>();
        Map<Peer, EnumSet<Peer.PeerRole>> joinedPeers = new HashMap<>();
        Map<Peer, EnumSet<Peer.PeerRole>> freePeers = new HashMap<>();
        Map<String, EnumSet<Peer.PeerRole>> peerList = CHANNEL_CONF.getPeerList(channelName);
        Collection<FabricOrg> orgs = ORG_CONF.getOrgs();
        orgs.forEach(org -> peerList.forEach((domain, roles) -> {
            Peer peer = createPeer(client, org, domain);
            if (peer != null) {
                if (construct || !isJoinedToChannel(client, peer, channelName)) {
                    freePeers.put(peer, roles);
                } else {
                    joinedPeers.put(peer, roles);
                }
            }
        }));
        map.put("joined", joinedPeers);
        map.put("free", freePeers);
        return map;
    }

    private static Peer createPeer(HFClient client, FabricOrg org, String domain) {
        String peerLocation = org.getPeerLocation(domain);
        // 当peer不属于当前机构时，会获取不到peerLocation
        if (peerLocation != null) {
            try {
                return client.newPeer(domain, peerLocation, TLS_CONF.getTlsProperties(domain));
            } catch (InvalidArgumentException e) {
                log.error("Failed to create peer {}", domain, e);
            } catch (IOException e) {
                log.error("Failed to create peer {}, because failed to read cert file.", domain, e);
            }
        }
        return null;
    }

    private static boolean isJoinedToChannel(HFClient client, Peer peer, String channelName) {
        // Query the actual peer for which channels it belongs to and check it belongs to this channel
        Set<String> channels = null;
        try {
            channels = client.queryChannels(peer);
        } catch (Exception e) {
            log.error("Failed to query channels {} on peer {}. Peer is ignored.", channelName,
                    peer.getName(), ThrowableUtil.findRootCause(e));
        }
        return channels != null && channels.contains(channelName);
    }

    private static void reregisterListeners(Channel channel) {
        // 移除已注册的监听器
        try {
            Class<? extends Channel> clazz = channel.getClass();
            Field field = clazz.getDeclaredField("chainCodeListeners");
            field.setAccessible(true);
            LinkedHashMap<String, ?> value = (LinkedHashMap<String, ?>) field.get(channel);
            if (value != null && !value.isEmpty()) {
                log.info("Clearing chaincode event listener. Size: {}", value.size());
                value.forEach((k, v) -> {
                    try {
                        channel.unregisterChaincodeEventListener(k);
                    } catch (InvalidArgumentException e) {
                        log.error("Failed to unregister chaincode event listener: {}", k);
                    }
                });
            }
            field = clazz.getDeclaredField("blockListeners");
            field.setAccessible(true);
            value = (LinkedHashMap<String, ?>) field.get(channel);
            if (value != null && value.isEmpty()) {
                log.info("Clearing block event listener. Size: {}", value.size());
                value.forEach((k, v) -> {
                    try {
                        channel.unregisterBlockListener(k);
                    } catch (InvalidArgumentException e) {
                        log.error("Failed to unregister block listener: {}", k);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to clear chaincode event listener.", e);
        }
        // 重新注册
        try {
            String handel = channel.registerBlockListener(new BlockCreatedListener());
            log.info("Block listener {} is registered on channel {}", handel, channel.getName());
        } catch (InvalidArgumentException e) {
            log.error("Failed to register chaincode event listener.", e);
        }
    }

}
