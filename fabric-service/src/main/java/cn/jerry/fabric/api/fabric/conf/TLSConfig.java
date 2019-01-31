package cn.jerry.fabric.api.fabric.conf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.helper.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class TLSConfig {
    private static final Logger logger = LogManager.getLogger();

    private static final String CONF_FILE = "/tls/tls.properties";
    private static final String TLS_FILE_DIR = LocalPathTool.removeWindowsDrive(
            TLSConfig.class.getResource("/tls").getPath());

    private static final String KEY_RUNNING_TLS = "running.tls";
    private static final String KEY_CA_ALLOW_ALL_HOSTNAMES = "CA_ALLOW_ALL_HOSTNAMES";
    private static final String PROTOCAL_HTTP = "http://";
    private static final String PROTOCAL_HTTPS = "https://";
    private static final String PROTOCAL_GRPC = "grpc://";
    private static final String PROTOCAL_GRPCS = "grpcs://";

    private static TLSConfig instance = new TLSConfig();
    private final Properties cache = new Properties();

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
            throw new RuntimeException(String.format("Bad TEST parameters for grpc url %s", location), e);
        }
        return location;
    }

    public String httpTLSify(String location) {
        location = location.trim();
        return (isRunningFabricCATLS() ? PROTOCAL_HTTPS : PROTOCAL_HTTP) + location;
    }

    public File getOrgCaCert(String orgDomain) throws FileNotFoundException {
        File certFile = new File(String.format("%s/ca.%s-cert.pem", TLS_FILE_DIR, orgDomain));
        if (!certFile.exists() || !certFile.isFile()) {
            throw new FileNotFoundException("Missing org ca cert file " + certFile.getAbsolutePath());
        }
        return certFile;
    }

    public Properties getTlsProperties(String hostname) throws FileNotFoundException {
        final Properties tlsProperties = new Properties();
        if (isRunningFabricCATLS()) {
            tlsProperties.setProperty("pemFile", getTlsFile(hostname).getAbsolutePath());
            //tlsProperties.setProperty("clientKeyFile", getTlsFile("/orderer-client.key").getAbsolutePath());
            //tlsProperties.setProperty("clientCertFile", getTlsFile("/orderer-client.crt").getAbsolutePath());
            tlsProperties.setProperty("hostnameOverride", hostname);
            tlsProperties.setProperty("sslProvider", "openSSL");
            tlsProperties.setProperty("negotiationType", "TLS");
        }
        return tlsProperties;
    }

    /**
     * org/ordererOrganizations/${orgDomainName}/orderers/${ordererHostName}/tls/server.crt
     * org/peerOrganizations/${orgDomainName}/peers/${peerHostName}/tls/server.crt
     *
     * @param hostname
     * @return
     */
    private File getTlsFile(String hostname) throws FileNotFoundException {
        File certFile = new File(String.format("%s/%s.crt", TLS_FILE_DIR, hostname));
        if (!certFile.exists() || !certFile.isFile()) {
            throw new FileNotFoundException("Missing tls file " + certFile.getAbsolutePath());
        }
        return certFile;
    }

}
