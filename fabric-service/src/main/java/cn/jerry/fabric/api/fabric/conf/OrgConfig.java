package cn.jerry.fabric.api.fabric.conf;

import cn.jerry.fabric.api.fabric.conf.model.LocationDef;
import cn.jerry.fabric.api.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.fabric.api.fabric.model.SampleOrg;
import cn.jerry.fabric.api.fabric.model.SampleUser;
import cn.jerry.fabric.api.fabric.service.ClientTool;
import cn.jerry.fabric.api.fabric.util.crypto.DigestUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

public class OrgConfig {
    private static final Logger logger = LogManager.getLogger();

    private static final String CONF_FILE = "/org/org.properties";
    private static final String ORG_FILE_DIR = LocalPathTool.removeWindowsDrive(
            OrgConfig.class.getResource("/org").getPath());

    private static final String IMAGE_VERSION = "1.3.0";

    private static final String KEY_ORG_NAMES = "org.names";
    private static final String KEY_ORG_MSP_ID = "%s.mspid";
    private static final String KEY_ORG_DOMAIN = "%s.domain";
    private static final String KEY_ORG_CA_NAME = "%s.ca.name";
    private static final String KEY_ORG_CA_LOCATION = "%s.ca.location";
    private static final String KEY_ORG_CA_ADMIN_NAME = "%s.ca.admin.name";
    private static final String KEY_ORG_CA_ADMIN_SECRET = "%s.ca.admin.secret";
    private static final String KEY_ORG_PEER_NAMES = "%s.peer.names";
    private static final String KEY_ORG_PEER_DOMAIN = "%s.%s.domain";
    private static final String KEY_ORG_PEER_LOCATION = "%s.%s.location";

    private static OrgConfig instance = new OrgConfig();
    private final TLSConfig tlsConfig = TLSConfig.getInstance();
    private final int[] fabricVersion = new int[3];
    private final Properties cache = new Properties();
    private final HashMap<String, SampleOrg> sampleOrgs = new HashMap<>();
    private SampleOrg defaultOrg;

    private OrgConfig() {
        final String[] fvs = IMAGE_VERSION.split("\\.");
        if (fvs.length != 3) {
            throw new AssertionError("Expected environment variable 'IMAGE_VERSION' to be three numbers" +
                    " sperated by dots (1.0.0)  but got: " + IMAGE_VERSION);
        }
        fabricVersion[0] = Integer.parseInt(fvs[0].trim());
        fabricVersion[1] = Integer.parseInt(fvs[1].trim());
        fabricVersion[2] = Integer.parseInt(fvs[2].trim());

        try {
            cache.load(OrgConfig.class.getResourceAsStream(CONF_FILE));
        } catch (IOException e) {
            logger.error("load properties failed. file: {}", CONF_FILE, e);
        }

        loadSampleOrgs();
    }

    public static OrgConfig getInstance() {
        return instance;
    }

    public boolean isRunningAgainstFabric10() {
        return isFabricVersionBefore("1.1");
    }

    public boolean isFabricVersionBefore(String version) {
        return !isFabricVersionAtOrAfter(version);
    }

    public boolean isFabricVersionAtOrAfter(String version) {
        final int[] vers = parseVersion(version);
        for (int i = 0; i < 3; ++i) {
            if (vers[i] > fabricVersion[i]) {
                return false;
            }
        }
        return true;
    }

    private static int[] parseVersion(String version) {
        if (null == version || version.isEmpty()) {
            throw new AssertionError("Version is bad :" + version);
        }
        String[] split = version.split("[ \\t]*\\.[ \\t]*");
        if (split.length < 1 || split.length > 3) {
            throw new AssertionError("Version is bad :" + version);
        }
        int[] ret = new int[3];
        int i = 0;
        for (; i < split.length; ++i) {
            ret[i] = Integer.parseInt(split[i]);
        }
        for (; i < 3; ++i) {
            ret[i] = 0;
        }
        return ret;
    }

    public Collection<SampleOrg> getSampleOrgs() {
        return Collections.unmodifiableCollection(sampleOrgs.values());
    }

    public SampleOrg getDefaultOrg() {
        return defaultOrg;
    }

    public SampleOrg getSampleOrg(String orgName) {
        return sampleOrgs.get(orgName);
    }

    private void loadSampleOrgs() {
        String orgNameStr = cache.getProperty(KEY_ORG_NAMES);
        if (orgNameStr == null || orgNameStr.isEmpty()) return;
        String[] orgNames = orgNameStr.split("[ \t]*,[ \t]*");
        String mspId, domainName, caName;
        LocationDef[] peersDef, eventHubsDef;
        for (String orgName : orgNames) {
            mspId = cache.getProperty(String.format(KEY_ORG_MSP_ID, orgName));
            final SampleOrg sampleOrg = new SampleOrg(orgName, mspId);
            sampleOrgs.put(orgName, sampleOrg);

            domainName = cache.getProperty(String.format(KEY_ORG_DOMAIN, orgName));
            sampleOrg.setDomainName(domainName);

            peersDef = loadGrpcLocations(KEY_ORG_PEER_NAMES, KEY_ORG_PEER_DOMAIN, KEY_ORG_PEER_LOCATION, orgName);
            for (LocationDef peer : peersDef) {
                sampleOrg.addPeerLocation(peer.getDomain(), peer.getLocation());
            }

//            eventHubsDef = loadGrpcLocations(KEY_ORG_EVENT_HUB_NAMES, KEY_ORG_EVENT_HUB_DOMAIN,
//                    KEY_ORG_EVENT_HUB_LOCATION, orgName);
//            for (LocationDef eventHub : eventHubsDef) {
//                sampleOrg.addEventHubLocation(eventHub.getDomain(), eventHub.getLocation());
//            }

            caName = cache.getProperty(String.format(KEY_ORG_CA_NAME, orgName));
            sampleOrg.setCAName(caName);
            sampleOrg.setCALocation(tlsConfig.httpTLSify(cache.getProperty(String.format(KEY_ORG_CA_LOCATION, orgName))));
            if (tlsConfig.isRunningFabricCATLS()) {
                try {
                    File cf = tlsConfig.getOrgCaCert(domainName);
                    Properties properties = new Properties();
                    properties.setProperty("pemFile", cf.getAbsolutePath());
                    if (tlsConfig.isCaAllowAllHostnames()) {
                        //testing environment only, NOT FOR PRODUCTION!
                        properties.setProperty("allowAllHostNames", "true");
                    }
                    sampleOrg.setCAProperties(properties);
                } catch (Exception e) {
                    logger.error("setup tls properties failed on org {}.", orgName, e);
                }
            }

            try {
                if (caName != null && !caName.isEmpty()) {
                    sampleOrg.setCAClient(HFCAClient.createNewInstance(caName, sampleOrg.getCALocation(),
                            sampleOrg.getCAProperties()));
                } else {
                    sampleOrg.setCAClient(HFCAClient.createNewInstance(sampleOrg.getCALocation(),
                            sampleOrg.getCAProperties()));
                }
                logger.info("HFCAClient registered on org {}.", orgName);
            } catch (Exception e) {
                logger.error("setup ca client failed on org {}.", orgName, e);
            }
            try {
                setupCaAdmin(sampleOrg, cache.getProperty(String.format(KEY_ORG_CA_ADMIN_NAME, orgName)),
                        cache.getProperty(String.format(KEY_ORG_CA_ADMIN_SECRET, orgName)));
                setupPeerAdmin(sampleOrg);
            } catch (IOException e) {
                logger.error("setup peer admin failed on org {}.", orgName, e);
            } catch (InitializeCryptoSuiteException | EnrollmentException | InvalidArgumentException e) {
                logger.error("setup ca admin failed on org {}.", orgName, e);
            }
        }
        defaultOrg = sampleOrgs.size() > 0 ? sampleOrgs.get(orgNames[0]) : null;
    }

    private LocationDef[] loadGrpcLocations(String namesKey, String domainKey, String locationKey, String orgName) {
        String orgNameStr = cache.getProperty(String.format(namesKey, orgName));
        if (orgNameStr == null || orgNameStr.isEmpty()) return new LocationDef[0];

        String[] names = orgNameStr.split("[ \t]*,[ \t]*");
        LocationDef[] defs = new LocationDef[names.length];
        String domain, location;
        for (int i = 0; i < names.length; i++) {
            domain = cache.getProperty(String.format(domainKey, orgName, names[i]));
            location = cache.getProperty(String.format(locationKey, orgName, names[i]));
            location = tlsConfig.grpcTLSify(location);
            defs[i] = new LocationDef(domain, location);
        }
        return defs;
    }

    public void setupCaAdmin(SampleOrg org, String adminName, String adminPw)
            throws InitializeCryptoSuiteException, EnrollmentException, InvalidArgumentException {
        // The ca admin of this org --
        SampleUser caAdmin = new SampleUser(adminName, org.getMSPID());
        caAdmin.setSecretSha1(DigestUtil.sha1DigestAsHex(adminPw.getBytes(StandardCharsets.UTF_8)));
        HFCAClient ca = ClientTool.getHFCAClient(org);
        caAdmin.setEnrollment(ca.enroll(adminName, adminPw));

        org.setCaAdmin(caAdmin);
        logger.info("CA admin user is set on org {}.", org.getName());
    }

    public void setupPeerAdmin(SampleOrg org) throws IOException {
        //A special user that can create channels, join peers and install chaincode
        SampleUser peerAdmin = new SampleUser(org.getName() + "Admin", org.getMSPID(),
                getOrgAdminKeyStore(org.getDomainName()), getOrgAdminSignCert(org.getDomainName()));

        org.setPeerAdmin(peerAdmin);
        logger.info("Peer admin user is set on org {}.", org.getName());
    }

    private File getOrgAdminKeyStore(String orgDomain) throws FileNotFoundException {
        File keyFile = new File(String.format("%s/Admin@%s-key_sk", ORG_FILE_DIR, orgDomain));
        if (!keyFile.exists() || !keyFile.isFile()) {
            throw new FileNotFoundException("Missing org admin sk file " + keyFile.getAbsolutePath());
        }
        return keyFile;
    }

    public File getOrgAdminSignCert(String orgDomain) throws FileNotFoundException {
        File certFile = new File(String.format("%s/Admin@%s-cert.pem", ORG_FILE_DIR, orgDomain));
        if (!certFile.exists() || !certFile.isFile()) {
            throw new FileNotFoundException("Missing org admin cert file " + certFile.getAbsolutePath());
        }
        return certFile;
    }

}
