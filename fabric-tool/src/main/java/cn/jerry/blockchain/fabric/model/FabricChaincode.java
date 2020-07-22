package cn.jerry.blockchain.fabric.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.TransactionRequest;

@JsonIgnoreProperties(value = {"id", "sourceBytes"})
public class FabricChaincode {

    private final ChaincodeID.Builder idBuilder;
    private ChaincodeID id;
    private TransactionRequest.Type lang;
    private byte[] sourceBytes;

    public FabricChaincode() {
        this.idBuilder = ChaincodeID.newBuilder();
    }

    public FabricChaincode(String name, String version, TransactionRequest.Type lang, String installPath,
            byte[] sourceBytes) {
        this();
        this.idBuilder.setName(name).setVersion(version);
        this.lang = lang;
        if (TransactionRequest.Type.GO_LANG == lang && installPath != null) {
            this.idBuilder.setPath(installPath);
        }
        this.sourceBytes = sourceBytes;
    }

    public ChaincodeID getId() {
        if (id == null) {
            id = this.idBuilder.build();
        }
        return id;
    }

    public String getName() {
        return getId().getName();
    }

    public void setName(String name) {
        this.id = null;
        this.idBuilder.setName(name);
    }

    public String getVersion() {
        return getId().getVersion();
    }

    public void setVersion(String version) {
        this.id = null;
        this.idBuilder.setVersion(version);
    }

    public TransactionRequest.Type getLang() {
        return lang;
    }

    public void setLang(TransactionRequest.Type lang) {
        this.lang = lang;
    }

    public String getInstallPath() {
        return getId().getPath();
    }

    public void setInstallPath(String installPath) {
        this.id = null;
        this.idBuilder.setPath(installPath);
    }

    public byte[] getSourceBytes() {
        return sourceBytes;
    }

    public void setSourceBytes(byte[] sourceBytes) {
        this.sourceBytes = sourceBytes;
    }

    @Override
    public String toString() {
        return "FabricChaincode{" +
                "name='" + getName() + '\'' +
                ", version='" + getVersion() + '\'' +
                ", lang=" + lang +
                ", installPath='" + getInstallPath() + '\'' +
                '}';
    }

}
