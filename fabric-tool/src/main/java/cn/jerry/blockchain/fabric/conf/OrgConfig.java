package cn.jerry.blockchain.fabric.conf;

import cn.jerry.blockchain.fabric.conf.model.LocationDef;
import cn.jerry.blockchain.fabric.model.FabricOrg;
import cn.jerry.blockchain.fabric.model.FabricUser;
import cn.jerry.blockchain.util.PemFileUtil;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OrgConfig {
    private static final Logger logger = LoggerFactory.getLogger(OrgConfig.class);

    private static final String CONF_FILE = "/org/org.properties";
    private static final String ORG_FILE_DIR = "/org";

    private static final String KEY_ORG_NAMES = "org.names";
    private static final String KEY_ORG_MSP_ID = "%s.mspid";
    private static final String KEY_ORG_DOMAIN = "%s.domain";
    private static final String KEY_ORG_CA_NAME = "%s.ca.name";
    private static final String KEY_ORG_CA_LOCATION = "%s.ca.location";
    private static final String KEY_ORG_PEER_NAMES = "%s.peer.names";
    private static final String KEY_ORG_PEER_DOMAIN = "%s.%s.domain";
    private static final String KEY_ORG_PEER_LOCATION = "%s.%s.location";

    private final TLSConfig tlsConfig = TLSConfig.getInstance();
    private final Properties cache = new Properties();
    private final HashMap<String, FabricOrg> orgs = new HashMap<>();
    private static final OrgConfig instance = new OrgConfig();

    private OrgConfig() {
        try {
            cache.load(OrgConfig.class.getResourceAsStream(CONF_FILE));
        } catch (IOException e) {
            logger.error("load properties failed. file: {}", CONF_FILE, e);
        }

        loadOrgs();
    }

    public static OrgConfig getInstance() {
        return instance;
    }

    public Collection<FabricOrg> getOrgs() {
        Collection<FabricOrg> values = new ArrayList<>();
        orgs.forEach((n, o) -> {
            if (n != null) {
                values.add(o);
            }
        });
        return Collections.unmodifiableCollection(values);
    }

    public FabricOrg getDefaultOrg() {
        return orgs.get(null);
    }

    public FabricOrg getOrg(String orgMspId) {
        return orgs.get(orgMspId);
    }

    private void loadOrgs() {
        String orgNameStr = cache.getProperty(KEY_ORG_NAMES);
        if (orgNameStr == null || orgNameStr.isEmpty()) return;
        String[] orgNames = orgNameStr.split("[ \t]*,[ \t]*");
        String mspId;
        String domainName;
        LocationDef[] peersDef;
        FabricOrg defaultOrg = null;
        for (String orgName : orgNames) {
            mspId = cache.getProperty(String.format(KEY_ORG_MSP_ID, orgName));
            domainName = cache.getProperty(String.format(KEY_ORG_DOMAIN, orgName));
            final FabricOrg org = new FabricOrg(orgName, mspId, domainName);
            orgs.put(mspId, org);
            if (defaultOrg == null) {
                defaultOrg = org;
                orgs.put(null, org);
            }

            peersDef = loadGrpcLocations(orgName);
            for (LocationDef peer : peersDef) {
                org.addPeerLocation(peer.getDomain(), peer.getLocation());
            }

            setupCAClient(org);

            try {
                setupPeerAdmin(org);
            } catch (IOException e) {
                logger.error("setup peer admin failed on org {}.", orgName, e);
            }
        }
    }

    private void setupCAClient(final FabricOrg org) {
        String caName = cache.getProperty(String.format(KEY_ORG_CA_NAME, org.getName()));
        org.setCAName(caName);
        org.setCALocation(tlsConfig.httpTLSify(cache.getProperty(String.format(KEY_ORG_CA_LOCATION, org.getName()))));
        if (tlsConfig.isRunningFabricCATLS()) {
            try {
                Properties properties = new Properties();
                // properties.setProperty("pemFile", tlsConfig.getOrgCaCert(domainName))
                properties.put("pemBytes", tlsConfig.getOrgCaCert(org.getDomainName()));
                if (tlsConfig.isCaAllowAllHostnames()) {
                    properties.setProperty("allowAllHostNames", Boolean.toString(tlsConfig.isCaAllowAllHostnames()));
                }
                org.setCAProperties(properties);
            } catch (Exception e) {
                logger.error("setup tls properties failed on org {}.", org.getName(), e);
            }
        }

        try {
            org.setCAClient(HFCAClient.createNewInstance(caName, org.getCALocation(),
                    org.getCAProperties()));
            logger.info("HFCAClient registered on org {}.", org.getName());
        } catch (Exception e) {
            logger.error("setup ca client failed on org {}.", org.getName(), e);
        }
    }

    private LocationDef[] loadGrpcLocations(String orgName) {
        String orgNameStr = cache.getProperty(String.format(OrgConfig.KEY_ORG_PEER_NAMES, orgName));
        if (orgNameStr == null || orgNameStr.isEmpty()) return new LocationDef[0];

        String[] names = orgNameStr.split("[ \t]*,[ \t]*");
        LocationDef[] defs = new LocationDef[names.length];
        String domain;
        String location;
        for (int i = 0; i < names.length; i++) {
            domain = cache.getProperty(String.format(OrgConfig.KEY_ORG_PEER_DOMAIN, orgName, names[i]));
            location = cache.getProperty(String.format(OrgConfig.KEY_ORG_PEER_LOCATION, orgName, names[i]));
            location = tlsConfig.grpcTLSify(location);
            defs[i] = new LocationDef(domain, location);
        }
        return defs;
    }

    private void setupPeerAdmin(FabricOrg org) throws IOException {
        //A special user that can create channels, join peers and install chaincode
        FabricUser peerAdmin = new FabricUser(org.getName() + "Admin", org.getMSPID(),
                getPeerAdminCertPem(org.getDomainName()), getPeerAdminKeyPem(org.getDomainName()));
        org.setPeerAdmin(peerAdmin);
        logger.info("Peer admin user is set on org {}.", org.getName());
    }

    private String getPeerAdminKeyPem(String orgDomain) throws IOException {
        try (InputStream stream = TLSConfig.class.getResourceAsStream(String.format("%s/Admin@%s-key_sk", ORG_FILE_DIR, orgDomain))) {
            byte[] bytes = PemFileUtil.readPemBytes(stream);
            return bytes.length > 0 ? new String(bytes, StandardCharsets.UTF_8) : "";
        }
    }

    public String getPeerAdminCertPem(String orgDomain) throws IOException {
        try (InputStream stream = TLSConfig.class.getResourceAsStream(String.format("%s/Admin@%s-cert.pem", ORG_FILE_DIR, orgDomain))) {
            byte[] bytes = PemFileUtil.readPemBytes(stream);
            return bytes.length > 0 ? new String(bytes, StandardCharsets.UTF_8) : "";
        }
    }

}
