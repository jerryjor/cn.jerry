package cn.jerry.blockchain.fabric.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ChaincodeConfig {
    private static final Logger logger = LoggerFactory.getLogger(ChaincodeConfig.class);

    private static final String CONF_FILE = "/chaincode/chaincode.properties";
    private static final String POLICY_FILE_DEFAULT = "/chaincode/policy.default.yaml";

    private static final String KEY_DEPLOY_TIMEOUT = "deploy.timeout";
    private static final String KEY_PROPOSAL_TIMEOUT = "proposal.timeout";
    private static final String KEY_INVOKE_TIMEOUT = "invoke.timeout";

    private final Properties cache = new Properties();
    private static final ChaincodeConfig instance = new ChaincodeConfig();

    private ChaincodeConfig() {
        try {
            cache.load(ChaincodeConfig.class.getResourceAsStream(CONF_FILE));
        } catch (IOException e) {
            logger.error("load properties failed. file: {}", CONF_FILE, e);
        }
    }

    public static ChaincodeConfig getInstance() {
        return instance;
    }

    public InputStream getDefaultEndorsementPolicy() {
        return ChaincodeConfig.class.getResourceAsStream(POLICY_FILE_DEFAULT);
    }

    public int getDeployTimeout() {
        return Integer.parseInt(cache.getProperty(KEY_DEPLOY_TIMEOUT, "120000"));
    }

    public int getProposalTimeout() {
        return Integer.parseInt(cache.getProperty(KEY_PROPOSAL_TIMEOUT, "300000"));
    }

    public int getInvokeTimeout() {
        return Integer.parseInt(cache.getProperty(KEY_INVOKE_TIMEOUT, "120000"));
    }

}
