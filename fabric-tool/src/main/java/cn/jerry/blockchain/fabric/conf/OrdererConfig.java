package cn.jerry.blockchain.fabric.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class OrdererConfig {
    private static final Logger logger = LoggerFactory.getLogger(OrdererConfig.class);

    private static final String CONF_FILE = "/orderer/orderer.properties";
    private static final String KEY_ORDERER_NAMES = "orderer.names";
    private static final String KEY_ORDERER_DOMAIN = "orderer.%s.domain";
    private static final String KEY_ORDERER_LOCATION = "orderer.%s.location";

    private final TLSConfig tlsConfig = TLSConfig.getInstance();
    private final Properties cache = new Properties();
    private final HashMap<String, String> ordererLocations = new HashMap<>();
    private static final OrdererConfig instance = new OrdererConfig();

    private OrdererConfig() {
        try {
            cache.load(OrdererConfig.class.getResourceAsStream(CONF_FILE));
        } catch (IOException e) {
            logger.error("load properties failed. file: {}", CONF_FILE, e);
        }

        loadOrdererLocations();
    }

    private void loadOrdererLocations() {
        String orgNameStr = cache.getProperty(KEY_ORDERER_NAMES);
        if (orgNameStr == null || orgNameStr.isEmpty()) return;

        String[] names = orgNameStr.split("[ \t]*,[ \t]*");
        String domain;
        String location;
        for (String name : names) {
            domain = cache.getProperty(String.format(KEY_ORDERER_DOMAIN, name));
            location = cache.getProperty(String.format(KEY_ORDERER_LOCATION, name));
            location = tlsConfig.grpcTLSify(location);
            ordererLocations.put(domain, location);
        }
    }

    public static OrdererConfig getInstance() {
        return instance;
    }

    public Map<String, String> getOrdererLocations() {
        return Collections.unmodifiableMap(ordererLocations);
    }

}
