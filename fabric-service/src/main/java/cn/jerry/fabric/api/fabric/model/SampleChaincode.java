package cn.jerry.fabric.api.fabric.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.TransactionRequest;

@JsonIgnoreProperties(value = {"id", "sourceBytes"})
public class SampleChaincode {

    private ChaincodeID id;
    private String name;
    private String version;
    private TransactionRequest.Type lang;
    private String installPath;
    private byte[] sourceBytes;

    public SampleChaincode() {
    }

    public SampleChaincode(String name, String version, TransactionRequest.Type lang, String installPath, byte[] sourceBytes) {
        this.name = name;
        this.version = version;
        this.lang = lang;
        this.installPath = installPath;
        this.sourceBytes = sourceBytes;
    }

    public ChaincodeID getId() {
        if (id == null) {
            genId();
        }
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public TransactionRequest.Type getLang() {
        return lang;
    }

    public void setLang(TransactionRequest.Type lang) {
        this.lang = lang;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(String installPath) {
        this.installPath = installPath;
    }

    public byte[] getSourceBytes() {
        return sourceBytes;
    }

    public void setSourceBytes(byte[] sourceBytes) {
        this.sourceBytes = sourceBytes;
    }

    @Override
    public String toString() {
        return "SampleChaincode{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", lang=" + lang +
                ", installPath='" + installPath + '\'' +
                '}';
    }

    private void genId() {
        // 数据不足时不生成id
        if (this.name == null || this.name.isEmpty() || this.version == null || this.version.isEmpty()
                || (TransactionRequest.Type.GO_LANG == this.lang
                && (this.installPath == null || this.installPath.isEmpty()))) return;

        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(this.name).setVersion(this.version);
        if (TransactionRequest.Type.GO_LANG == lang) {
            chaincodeIDBuilder.setPath(this.installPath);
        }
        this.id = chaincodeIDBuilder.build();
    }

}
