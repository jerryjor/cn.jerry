package cn.jerry.blockchain.fabric.event;

import cn.jerry.blockchain.fabric.cache.EventCache;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.BlockListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlockCreatedListener implements BlockListener {
    private static final Logger logger = LoggerFactory.getLogger(BlockCreatedListener.class);

    private static final List<Class<? extends IBlockEventListener>> listeners = new ArrayList<>();

    public static void registerListener(Class<? extends IBlockEventListener> listenerClass) {
        if (listenerClass != null) {
            synchronized (listeners) {
                listeners.add(listenerClass);
            }
        }
    }

    @Override
    public void received(BlockEvent blockEvent) {
        blockEvent.getEnvelopeInfos().forEach(info -> {
            String channelName = info.getChannelId();
            String txId = info.getTransactionID();
            Date txTime = info.getTimestamp();
            boolean success = info.isValid() && info.getValidationCode() == 0;
            String ccName = null;
            String ccVersion = null;
            if (info.getType() == BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE) {
                BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo.TransactionEnvelopeInfo) info;
                for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo transactionActionInfo : transactionEnvelopeInfo
                        .getTransactionActionInfos()) {
                    ccName = transactionActionInfo.getChaincodeIDName();
                    ccVersion = transactionActionInfo.getChaincodeIDVersion();
                }
            }
            if ("lscc".equals(ccName)) {
                return;
            }
            final String chaincodeName = ccName;
            final String chaincodeVersion = ccVersion;
            if (chaincodeName != null) {
                boolean repeated = EventCache.getInstance(ccName).existsOrSave(txId);
                if (!repeated) {
                    logger.info("Received block info. ccName: {}, ccVersion: {}, transaction: {}, time: {}, status: {}",
                            ccName, ccVersion, txId, txTime, success);
                    // 发送数据给区块链服监听服务
                    listeners.forEach(listenerClass -> {
                        try {
                            IBlockEventListener listener = listenerClass.newInstance();
                            listener.onTransactionCompleted(channelName, chaincodeName, chaincodeVersion, txId, txTime.getTime(), success);
                        } catch (Exception | java.lang.NoClassDefFoundError | java.lang.IllegalAccessError | java.lang.ClassFormatError e) {
                            logger.error("Failed to call listener {}.", listenerClass.getName(), e);
                        }
                    });
                }
            }
        });
    }
}
