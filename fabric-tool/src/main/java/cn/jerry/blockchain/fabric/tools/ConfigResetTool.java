package cn.jerry.blockchain.fabric.tools;

import org.hyperledger.fabric.sdk.helper.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class ConfigResetTool {
    private static final Logger logger = LoggerFactory.getLogger(ConfigResetTool.class);

    private ConfigResetTool() {
        super();
    }

    /**
     * Reset clientConfig.
     */
    public static void resetConfig() {
        System.setProperty(Config.GENESISBLOCK_WAIT_TIME, "300000");
        try {
            final Field field = Config.class.getDeclaredField("config");
            field.setAccessible(true);
            field.set(Config.class, null);
            Config.getConfig();
        } catch (Exception e) {
            logger.error("Cannot reset clientConfig", e);
        }
    }

}
