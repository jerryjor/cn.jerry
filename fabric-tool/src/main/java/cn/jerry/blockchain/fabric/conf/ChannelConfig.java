package cn.jerry.blockchain.fabric.conf;

import cn.jerry.blockchain.fabric.model.FabricOrg;
import cn.jerry.blockchain.util.PemFileUtil;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ChannelConfig {
    private static final Logger logger = LoggerFactory.getLogger(ChannelConfig.class);

    private static final String CONF_FILE = "/channel/channel.properties";
    private static final String KEY_PUBLIC_CHANNEL_NAME = "channel.public.name";
    private static final String KEY_ENDORSER_LIST = "%s.endorser";
    private static final String KEY_COMMITTER_LIST = "%s.committer";
    private static final String KEY_GRPC_KEEPALIVE_TIME = "grpc.keep-alive.time";
    private static final String KEY_GRPC_KEEPALIVE_TIMEOUT = "grpc.keep-alive.timeout";
    private static final String KEY_GRPC_MAX_INBOUND_MESSAGE_SIZE = "grpc.inbound.message.size.max";

    private final Properties cache = new Properties();
    private final HashMap<String, byte[]> configuredChannels = new HashMap<>();
    private final HashMap<String, Channel> registeredChannels = new HashMap<>();
    private static final ChannelConfig instance = new ChannelConfig();

    private ChannelConfig() {
        try {
            cache.load(ChannelConfig.class.getResourceAsStream(CONF_FILE));
        } catch (IOException e) {
            logger.error("load properties failed. file: {}", CONF_FILE, e);
        }
    }
    
    public static ChannelConfig getInstance() {
        return instance;
    }

    public String getPublicChannelName() {
        return cache.getProperty(KEY_PUBLIC_CHANNEL_NAME);
    }

    public byte[] getChannelFile(String channelName) {
        // 已缓存，直接返回
        byte[] bytes = configuredChannels.get(channelName);
        if (bytes != null) {
            return bytes;
        }
        // 未缓存，读取tx文件并更新到缓存
        try (InputStream stream = ChannelConfig.class.getResourceAsStream(String.format("/channel/%s.tx", channelName))) {
            bytes = PemFileUtil.readPemBytes(stream);
            if (bytes.length > 0) {
                configuredChannels.put(channelName, bytes);
            }
        } catch (FileNotFoundException fof) {
            logger.info("Channel {} tx file not exists.", channelName);
        } catch (IOException e) {
            logger.info("Failed to read channel {} tx file.", channelName);
        }
        return bytes == null ? new byte[0] : bytes;
    }

    public Map<String, EnumSet<Peer.PeerRole>> getPeerList(String channelName) {
        EnumSet<Peer.PeerRole> committerRoles = EnumSet.of(Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.CHAINCODE_QUERY);
        EnumSet<Peer.PeerRole> endorserRoles = EnumSet.of(Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.CHAINCODE_QUERY,
                Peer.PeerRole.ENDORSING_PEER, Peer.PeerRole.EVENT_SOURCE);
        Map<String, EnumSet<Peer.PeerRole>> peers = new HashMap<>();

        String endorsers = cache.getProperty(String.format(KEY_ENDORSER_LIST, channelName));
        if (endorsers == null || endorsers.isEmpty()) return getAllPeers();
        String[] domainsArr = endorsers.split("[ \t]*,[ \t]*");
        for (String domain : domainsArr) {
            peers.put(domain, endorserRoles);
        }

        String committers = cache.getProperty(String.format(KEY_COMMITTER_LIST, channelName));
        if (committers == null || committers.isEmpty()) return peers;
        domainsArr = committers.split("[ \t]*,[ \t]*");
        for (String domain : domainsArr) {
            peers.put(domain, committerRoles);
        }

        return peers;
    }

    private Map<String, EnumSet<Peer.PeerRole>> getAllPeers() {
        Map<String, EnumSet<Peer.PeerRole>> peers = new HashMap<>();
        EnumSet<Peer.PeerRole> all = Peer.PeerRole.ALL;
        all.remove(Peer.PeerRole.SERVICE_DISCOVERY);
        for (FabricOrg org : OrgConfig.getInstance().getOrgs()) {
            org.getPeerNames().forEach( pn -> peers.put(pn, all));
        }
        return peers;
    }

    public long getGrpcKeepAliveTime() {
        return Long.parseLong(cache.getProperty(KEY_GRPC_KEEPALIVE_TIME, "300000"));
    }

    public long getGrpcKeepAliveTimeout() {
        return Long.parseLong(cache.getProperty(KEY_GRPC_KEEPALIVE_TIMEOUT, "80000"));
    }

    public int getGrpcMaxInboundMessageSize() {
        return Integer.parseInt(cache.getProperty(KEY_GRPC_MAX_INBOUND_MESSAGE_SIZE, "80000"));
    }

    public void registerChannel(Channel channel) {
        registeredChannels.put(channel.getName(), channel);
    }

    public Channel getRegisteredChannel(String name) {
        return registeredChannels.get(name);
    }

    public Map<String, Channel> getAllRegisteredChannels() {
        return registeredChannels;
    }

}
