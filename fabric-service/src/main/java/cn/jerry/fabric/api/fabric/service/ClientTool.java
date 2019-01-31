package cn.jerry.fabric.api.fabric.service;

import cn.jerry.fabric.api.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.fabric.api.fabric.model.SampleOrg;
import cn.jerry.fabric.api.fabric.model.SampleUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

public class ClientTool {
    private static Logger log = LogManager.getLogger();

    public static HFClient getUserHFClient(SampleUser user) throws InitializeCryptoSuiteException {
        HFClient client = user.getClient();
        if (client == null) {
            client = HFClient.createNewInstance();
            try {
                client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
                client.setUserContext(user);
            } catch (Exception e) {
                throw new InitializeCryptoSuiteException(e);
            }
            user.setClient(client);
        }
        return client;
    }

    public static HFCAClient getHFCAClient(SampleOrg org) throws InitializeCryptoSuiteException {
        HFCAClient caClient = org.getCAClient();
        if (caClient.getCryptoSuite() == null) {
            try {
                caClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            } catch (Exception e) {
                throw new InitializeCryptoSuiteException(e);
            }
        }
        return caClient;
    }

}
