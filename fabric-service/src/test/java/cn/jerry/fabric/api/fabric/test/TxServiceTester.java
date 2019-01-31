package cn.jerry.fabric.api.fabric.test;

import cn.jerry.fabric.api.fabric.model.SampleChaincode;
import cn.jerry.fabric.api.fabric.model.SampleOrg;
import cn.jerry.fabric.api.fabric.service.ChannelService;
import cn.jerry.fabric.api.fabric.service.ClientTool;
import cn.jerry.fabric.api.fabric.service.TransactionService;
import cn.jerry.fabric.api.fabric.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.TransactionRequest;

public class TxServiceTester {
    private static Logger log = LogManager.getLogger();

    private static UserService userService = new UserService();
    private static ChannelService channelService = ChannelService.getInstance();
    private static TransactionService txService = new TransactionService();

    public static void main(String[] args) {
        SampleChaincode ccDef1 = new SampleChaincode("testing", "2.0", TransactionRequest.Type.JAVA, null, null);
        Channel channel1 = null;
        try {
            SampleOrg org = userService.getDefaultOrg();
            channel1 = channelService.getChannel(org, ccDef1);
            // channel2 = channelService.getChannel(user, ccDef2);

            ClientTool.getUserHFClient(org.getPeerAdmin()).getChannel("");

            // 执行交易
            String[] params = new String[]{"account1", "2"};
            String txId1 = txService.doTransaction(org.getPeerAdmin(), channel1, ccDef1.getId(), "update", params, false);
            String txId2 = txService.doTransaction(org.getPeerAdmin(), channel1, ccDef1.getId(), "update", params, false);
            System.out.println(txId1);
            System.out.println(txId2);
            // 异步发送交易，等一下
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException ie) {
                // do nothing...
            }

            // 根据交易ID查询交易信息
            System.out.println(txService.queryTxValidationCode(org.getPeerAdmin(), channel1, txId1));
            String result1 = txService.doQuery(org.getPeerAdmin(), channel1, ccDef1.getId(), "queryTx", new String[]{txId1});
            System.out.println(result1);
            System.out.println(txService.queryTxValidationCode(org.getPeerAdmin(), channel1, txId2));
            String result2 = txService.doQuery(org.getPeerAdmin(), channel1, ccDef1.getId(), "queryTx", new String[]{txId2});
            System.out.println(result2);

            // 查询历史交易列表
            // String result1 = txService.doQuery(org.getPeerAdmin(), channel1, ccDef1.getId(), "queryHisTx", new String[]{"account1", "1"});
            // System.out.println("=============================");
            // System.out.println(result1);
            // System.out.println("=============================");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channel1 != null) {
                channel1.shutdown(false);
            }
        }
    }

}
