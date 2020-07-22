package cn.jerry.blockchain.fabric.event;

public interface IBlockEventListener {

    void onTransactionCompleted(String channelName, String chaincodeName, String chaincodeVersion, String transactionID,
            Long transactionCompleteTime, boolean success);

}
