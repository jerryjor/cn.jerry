package cn.jerry.fabric.api.fabric.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.helper.Config;

import java.lang.reflect.Field;

public abstract class AbstractChaincodeService<T> {
    protected Logger log = LogManager.getLogger(this.getClass());

    public AbstractChaincodeService() {
        resetConfig();
    }

    /**
     * Reset clientConfig.
     */
    private void resetConfig() {
        System.setProperty(Config.GENESISBLOCK_WAIT_TIME, "300000");
        try {
            final Field field = Config.class.getDeclaredField("config");
            field.setAccessible(true);
            field.set(Config.class, null);
            Config.getConfig();
        } catch (Exception e) {
            throw new RuntimeException("Cannot reset clientConfig", e);
        }
    }

}
