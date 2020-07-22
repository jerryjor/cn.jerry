package cn.jerry.fabric.api.fabric.test;

import cn.jerry.blockchain.fabric.conf.ChannelConfig;
import cn.jerry.blockchain.fabric.conf.OrgConfig;
import cn.jerry.blockchain.fabric.exception.InitializeCryptoSuiteException;
import cn.jerry.blockchain.fabric.model.FabricOrg;
import cn.jerry.blockchain.fabric.tools.ChannelTools;
import cn.jerry.blockchain.fabric.tools.ClientTool;
import org.hyperledger.fabric.protos.common.Configtx;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

import java.util.ArrayList;
import java.util.List;

public class ChannelTester {
    public static void main(String[] args) {
        ChaincodeID id = ChaincodeID.newBuilder().setName("merchant_contract").setVersion("2.0").build();
        FabricOrg org = OrgConfig.getInstance().getDefaultOrg();
        try {
//            String channelName = ChannelConfig.getInstance().getPublicChannelName();
//            byte[] txFile = ChannelConfig.getInstance().getChannelFile(channelName);
//            UpdateChannelConfiguration updateCfg = new UpdateChannelConfiguration(txFile);
//            byte[][] signatures = getAdminSignatures(updateCfg);
//            System.out.println(signatures.length);
//            HFClient client = ClientTool.getUserHFClient(OrgConfig.getInstance().getDefaultOrg().getPeerAdmin());
            // Channel newChannel = client.newChannel(channelName);
            Channel newChannel = ChannelTools.getChannel(org, id);
//            newChannel.updateChannelConfiguration(updateCfg, signatures);
            System.out.println(newChannel.getPeers().iterator().next().getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[][] getAdminSignatures(UpdateChannelConfiguration updateCfg) {
        List<byte[]> signatures = new ArrayList<>();
        for (FabricOrg org : OrgConfig.getInstance().getOrgs()) {
            System.out.println(org.getName());
            if (org.getPeerAdmin() != null) {
                try {
                    HFClient client = ClientTool.getUserHFClient(org.getPeerAdmin());
                    signatures.add(client.getUpdateChannelConfigurationSignature(updateCfg, org.getPeerAdmin()));
                } catch (InvalidArgumentException e) {
                    System.out.println("Failed to get channel configuration signature.");
                } catch (InitializeCryptoSuiteException e) {
                    System.out.println("Failed to create HFClient.");
                }
            } else {
                System.out.println("Peer admin is NOT registered on org " + org.getName());
            }
        }
        return signatures.toArray(new byte[signatures.size()][]);
    }

}
