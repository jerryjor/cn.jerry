package cn.jerry.blockchain.fabric.tools;

import cn.jerry.blockchain.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.blockchain.fabric.model.FabricOrg;
import cn.jerry.blockchain.fabric.model.FabricUser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.util.Properties;

public class ClientTool {
    private static final String PROVIDER_CLASS = BouncyCastleProvider.class.getName();

    private ClientTool() {
        super();
    }

    public static HFClient getUserHFClient(FabricUser user) throws InitializeCryptoSuiteException {
        HFClient client = HFClient.createNewInstance();
        try {
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(user);
        } catch (Exception e) {
            throw new InitializeCryptoSuiteException(e);
        }
        return client;
    }

    public static HFCAClient getHFCAClient(FabricOrg org) throws InitializeCryptoSuiteException {
        HFCAClient caClient = org.getCAClient();
        if (caClient.getCryptoSuite() == null) {
            Properties properties = new Properties();
            properties.put("org.hyperledger.fabric.sdk.security_provider_class_name", PROVIDER_CLASS);
            try {
                caClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite(properties));
            } catch (Exception e) {
                throw new InitializeCryptoSuiteException(e);
            }
        }
        return caClient;
    }

}
