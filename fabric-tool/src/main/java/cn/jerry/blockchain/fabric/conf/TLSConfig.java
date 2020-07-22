package cn.jerry.blockchain.fabric.conf;

import cn.jerry.blockchain.util.PemFileUtil;
import org.hyperledger.fabric.sdk.helper.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TLSConfig {
    private static final Logger logger = LoggerFactory.getLogger(TLSConfig.class);

    private static final String CONF_FILE = "/tls/tls.properties";
    private static final String KEY_RUNNING_TLS = "running.tls";
    private static final String KEY_CA_ALLOW_ALL_HOSTNAMES = "CA_ALLOW_ALL_HOSTNAMES";
    private static final String PROTOCAL_HTTP = "http://";
    private static final String PROTOCAL_HTTPS = "https://";
    private static final String PROTOCAL_GRPC = "grpc://";
    private static final String PROTOCAL_GRPCS = "grpcs://";

    private final Properties cache = new Properties();
    private static final TLSConfig instance = new TLSConfig();

    private TLSConfig() {
        try {
            cache.load(TLSConfig.class.getResourceAsStream(CONF_FILE));
        } catch (IOException e) {
            logger.error("load properties failed. file: {}", CONF_FILE, e);
        }
    }

    public static TLSConfig getInstance() {
        return instance;
    }

    public boolean isRunningFabricCATLS() {
        String value = cache.getProperty(KEY_RUNNING_TLS);
        return value != null && "1".equals(value.trim());
    }

    public boolean isCaAllowAllHostnames() {
        String value = cache.getProperty(KEY_CA_ALLOW_ALL_HOSTNAMES);
        return value != null && "1".equals(value.trim());
    }

    public String grpcTLSify(String location) {
        location = location.trim();
        location = (isRunningFabricCATLS() ? PROTOCAL_GRPCS : PROTOCAL_GRPC) + location;
        Exception e = Utils.checkGrpcUrl(location);
        if (e != null) {
            throw new IllegalArgumentException(String.format("Bad TEST parameters for grpc url %s", location), e);
        }
        return location;
    }

    public String httpTLSify(String location) {
        location = location.trim();
        return (isRunningFabricCATLS() ? PROTOCAL_HTTPS : PROTOCAL_HTTP) + location;
    }

    public byte[] getOrgCaCert(String orgDomain) throws IOException {
        try (InputStream stream = TLSConfig.class.getResourceAsStream(String.format("/tls/ca.%s-cert.pem", orgDomain))) {
            return PemFileUtil.readPemBytes(stream);
        }
    }

    public Properties getTlsProperties(String hostname) throws IOException {
        final Properties tlsProperties = new Properties();
        if (isRunningFabricCATLS()) {
            // org/ordererOrganizations/${orgDomainName}/orderers/${ordererHostName}/tls/server.crt
            // org/peerOrganizations/${orgDomainName}/peers/${peerHostName}/tls/server.crt
            try (InputStream stream = TLSConfig.class.getResourceAsStream(String.format("/tls/%s.crt", hostname))) {
                tlsProperties.put("pemBytes", PemFileUtil.readPemBytes(stream));
            }
            tlsProperties.setProperty("hostnameOverride", hostname);
            tlsProperties.setProperty("sslProvider", "openSSL");
            tlsProperties.setProperty("negotiationType", "TLS");
        }
        return tlsProperties;
    }

}
