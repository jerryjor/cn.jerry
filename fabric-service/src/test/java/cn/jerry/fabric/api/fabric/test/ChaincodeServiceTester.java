package cn.jerry.fabric.api.fabric.test;

import cn.jerry.fabric.api.fabric.model.SampleChaincode;
import cn.jerry.fabric.api.fabric.model.SampleOrg;
import cn.jerry.fabric.api.fabric.service.ChaincodeService;
import cn.jerry.fabric.api.fabric.service.ChannelService;
import cn.jerry.fabric.api.fabric.service.ClientTool;
import cn.jerry.fabric.api.fabric.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.helper.Utils;

import java.io.File;

public class ChaincodeServiceTester {
    private static Logger log = LogManager.getLogger();

    private static UserService userService = new UserService();
    private static ClientTool clientService = new ClientTool();
    private static ChannelService channelService = ChannelService.getInstance();
    private static ChaincodeService ccService = new ChaincodeService();

    public static void main(String[] args) {
        String orgName = "jerry";
        SampleChaincode ccDef1 = new SampleChaincode("testing", "1.0", TransactionRequest.Type.JAVA, null, null);
        String ccSourcePath = "........";
        Channel channel1 = null;
        try {
            SampleOrg org = userService.getDefaultOrg();

            channel1 = channelService.getChannel(org, ccDef1);

            // 安装链码
            ccDef1.setSourceBytes(Utils.generateTarGz(new File(ccSourcePath), "src", null));
            ccService.install(org, channel1, ccDef1);

//            // 实例化链码
//            Map<String, String> accountsInitAmount = new HashMap<>();
//            accountsInitAmount.put("account1", "10");
//            accountsInitAmount.put("account2", "200");
//            String txId1 = ccService.instantiate(org, channel1, ccDef1, accountsInitAmount);
//            System.out.println(txId1);

            // 升级链码
            ccDef1.setVersion("2.0");
            String txId22 = ccService.upgrade(org, channel1, ccDef1);
            System.out.println(txId22);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channel1 != null) {
                channel1.shutdown(false);
            }
        }
    }

}
