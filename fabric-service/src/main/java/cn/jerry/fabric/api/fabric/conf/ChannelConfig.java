package cn.jerry.fabric.api.fabric.conf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class ChannelConfig {
    private static final Logger logger = LogManager.getLogger();

    private static final String CONF_FILE = "/channel/channel.properties";
    private static final String CHANNEL_FILE_DIR = LocalPathTool.removeWindowsDrive(
            ChannelConfig.class.getResource("/channel").getPath());

    private static final String KEY_PUBLIC_CHANNEL_NAME = "channel.public.name";
    private static final String KEY_GRPC_KEEPALIVE_TIME = "grpc.keep-alive.time";
    private static final String KEY_GRPC_KEEPALIVE_TIMEOUT = "grpc.keep-alive.timeout";
    private static final String KEY_GRPC_MAX_INBOUND_MESSAGE_SIZE = "grpc.inbound.message.size.max";

    private static ChannelConfig instance = new ChannelConfig();
    private final Properties cache = new Properties();
    private final HashMap<String, Channel> registeredChannels = new HashMap<>();

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

    public File getChannelFile(String channelName) throws FileNotFoundException {
        File txFile = new File(String.format("%s/%s.tx", CHANNEL_FILE_DIR, channelName));
        if (!txFile.exists() || !txFile.isFile()) {
            throw new FileNotFoundException("Missing channel tx file: " + txFile.getAbsolutePath());
        }
        return txFile;
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
}
