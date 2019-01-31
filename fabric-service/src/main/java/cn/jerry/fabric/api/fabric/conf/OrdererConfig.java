package cn.jerry.fabric.api.fabric.conf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class OrdererConfig {
    private static final Logger logger = LogManager.getLogger();

    private static final String CONF_FILE = "/orderer/orderer.properties";
    private static final String ORDERER_FILE_DIR = LocalPathTool.removeWindowsDrive(
            OrdererConfig.class.getResource("/orderer").getPath());

    private static final String KEY_ORDERER_NAMES = "orderer.names";
    private static final String KEY_ORDERER_DOMAIN = "orderer.%s.domain";
    private static final String KEY_ORDERER_LOCATION = "orderer.%s.location";

    private static OrdererConfig instance = new OrdererConfig();
    private final TLSConfig tlsConfig = TLSConfig.getInstance();
    private final Properties cache = new Properties();
    private final HashMap<String, String> ordererLocations = new HashMap<>();

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
        String domain, location;
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
