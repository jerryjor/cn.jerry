package cn.jerry.blockchain.fabric.conf.model;

public class LocationDef {
    private String domain;
    private String location;

    public LocationDef(String domain, String location) {
        this.domain = domain;
        this.location = location;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "LocationDef{" +
                "domain='" + domain + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
