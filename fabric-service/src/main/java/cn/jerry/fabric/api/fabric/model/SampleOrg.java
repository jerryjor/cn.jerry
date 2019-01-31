package cn.jerry.fabric.api.fabric.model;

import cn.jerry.fabric.api.fabric.cache.UserCache;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.util.*;

/*
 *  Copyright 2016, 2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * Sample Organization Representation
 * <p>
 * Keeps track which resources are defined for the Organization it represents.
 */
public class SampleOrg {
    private final String name;
    private final String mspid;
    private String domainName;
    private Map<String, String> peerLocations = new HashMap<>();
    private Map<String, String> eventHubLocations = new HashMap<>();

    private String caName;
    private String caLocation;
    private Properties caProperties = null;
    private HFCAClient caClient;

    private UserCache userCache = UserCache.getInstance();
    private SampleUser caAdmin;
    private SampleUser peerAdmin;

    public SampleOrg(String name, String mspid) {
        this.name = name;
        this.mspid = mspid;
    }

    public String getName() {
        return name;
    }

    public String getMSPID() {
        return mspid;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getPeerLocation(String name) {
        return peerLocations.get(name);
    }

    public void addPeerLocation(String name, String location) {
        peerLocations.put(name, location);
    }

    public Set<String> getPeerNames() {
        return Collections.unmodifiableSet(peerLocations.keySet());
    }

    public String getEventHubLocation(String name) {
        return eventHubLocations.get(name);
    }

    public void addEventHubLocation(String name, String location) {
        eventHubLocations.put(name, location);
    }

    public Set<String> getEventHubNames() {
        return Collections.unmodifiableSet(eventHubLocations.keySet());
    }

    public Collection<String> getEventHubLocations() {
        return Collections.unmodifiableCollection(eventHubLocations.values());
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

    public SampleUser getCaAdmin() {
        return caAdmin;
    }

    public void setCaAdmin(SampleUser caAdmin) {
        this.caAdmin = caAdmin;
        addUser(caAdmin);
    }

    public void addUser(SampleUser user) {
        user.setAffiliation(this.name);
        userCache.put(user);
    }

    public SampleUser getUser(String name) {
        return userCache.lookup(name);
    }

    public void removeUser(String name) {
        userCache.invalidate(name);
    }

    public SampleUser getPeerAdmin() {
        return peerAdmin;
    }

    public void setPeerAdmin(SampleUser peerAdmin) {
        peerAdmin.setAffiliation(this.name);
        this.peerAdmin = peerAdmin;
    }

}
