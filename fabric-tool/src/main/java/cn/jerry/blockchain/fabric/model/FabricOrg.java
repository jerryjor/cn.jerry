package cn.jerry.blockchain.fabric.model;

import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.util.*;

/**
 * Sample Organization Representation
 * <p>
 * Keeps track which resources are defined for the Organization it represents.
 */
public class FabricOrg {
    private final String name;
    private final String mspid;
    private final String domainName;
    private final Map<String, String> peerLocations = new HashMap<>();

    private String caName;
    private String caLocation;
    private Properties caProperties = null;
    private HFCAClient caClient;

    private FabricUser caAdmin;
    private FabricUser peerAdmin;

    public FabricOrg(String name, String mspid, String domainName) {
        this.name = name;
        this.mspid = mspid;
        this.domainName = domainName;
    }

    public String getName() {
        return name;
    }

    public String getMSPID() {
        return mspid;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getPeerLocation(String domain) {
        return peerLocations.get(domain);
    }

    public void addPeerLocation(String domain, String location) {
        peerLocations.put(domain, location);
    }

    public Set<String> getPeerNames() {
        return Collections.unmodifiableSet(peerLocations.keySet());
    }

    public String getCAName() {
        return caName;
    }

    public void setCAName(String caName) {
        this.caName = caName;
    }

    public String getCALocation() {
        return this.caLocation;
    }

    public void setCALocation(String caLocation) {
        this.caLocation = caLocation;
    }

    public void setCAProperties(Properties caProperties) {
        this.caProperties = caProperties;
    }

    public Properties getCAProperties() {
        return caProperties;
    }

    public HFCAClient getCAClient() {
        return caClient;
    }

    public void setCAClient(HFCAClient caClient) {
        this.caClient = caClient;
    }

    public FabricUser getPeerAdmin() {
        return peerAdmin;
    }

    public void setPeerAdmin(FabricUser peerAdmin) {
        peerAdmin.setAffiliation(this.name);
        this.peerAdmin = peerAdmin;
    }

    @Override
    public String toString() {
        return "FabricOrg{" +
                "name='" + name + '\'' +
                ", mspid='" + mspid + '\'' +
                ", domainName='" + domainName + '\'' +
                '}';
    }
}
