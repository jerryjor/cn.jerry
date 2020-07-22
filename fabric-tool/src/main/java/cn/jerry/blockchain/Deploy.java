package cn.jerry.blockchain;

import cn.jerry.blockchain.fabric.conf.OrgConfig;
import cn.jerry.blockchain.fabric.model.FabricChaincode;
import cn.jerry.blockchain.fabric.model.FabricOrg;
import cn.jerry.blockchain.fabric.tools.ChaincodeTools;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.helper.Utils;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class Deploy {

    public static void main(String[] args) {
        Map<String, String> argParams = ArgsTool.readArgs(args);
        String an = argParams.get("an");
        String ap = argParams.get("ap");
        String ccn = argParams.get("ccn");
        String ccv = argParams.get("ccv");
        String ccl = argParams.get("ccl");
        String ccp = argParams.get("ccp");
        String ccf = argParams.get("ccf");
        if (an == null || ap == null || ccn == null || ccv == null || ccl == null
                || ("GO_LANG".equals(ccl) && ccp == null) || ccf == null) {
            System.out.println("参数不足. 例："
                    + "\n\t-an ca服务管理员用户名"
                    + "\n\t-ap ca服务管理员密码"
                    + "\n\t-ccn 链码名称"
                    + "\n\t-ccv 链码版本"
                    + "\n\t-ccl 链码语言[JAVA/GO_LANG/NODE]"
                    + "\n\t-ccf 链码源码路径[绝对路径，项目根目录]"
                    + "\n\t-ccp 链码安装路径[仅当语言为GO_LANG时需要]");
            return;
        }

        try {
            byte[] sourceBytes = Utils.generateTarGz(new File(ccf), "src", null);
            FabricChaincode chaincode = new FabricChaincode(ccn, ccv, TransactionRequest.Type.valueOf(ccl), ccp, sourceBytes);
            System.out.println("Deploy chaincode start, chaincode: " + chaincode);

            FabricOrg org = OrgConfig.getInstance().getDefaultOrg();
            ChaincodeTools.install(org, chaincode);
            String txId = ChaincodeTools.instantiate(org, chaincode, null);
            System.out.println("部署成功，交易ID：" + txId);
        } catch (Exception e) {
            System.err.println("部署失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
