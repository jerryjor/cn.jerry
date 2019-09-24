package cn.jerry.net;

import cn.jerry.logging.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class BlankHostVerifier implements HostnameVerifier {
    private static Logger logger = LogManager.getLogger();

    @Override
    public boolean verify(String hostname, SSLSession session) {
        if (hostname == null) {
            logger.info("hostname is null, refused.");
            return false;
        } else {
            return true;
        }
    }

}
