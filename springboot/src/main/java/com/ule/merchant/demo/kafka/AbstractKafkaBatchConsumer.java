package com.ule.merchant.demo.kafka;

import com.ule.merchant.demo.util.JsonUtil;
import com.ule.tools.client.kafka.consumer.Consumer;
import com.ule.tools.client.kafka.consumer.handler.IConsumerBatchHandler;
import com.ule.tools.client.kafka.consumer.handler.KeyValuePair;
import com.ule.tools.client.kafka.core.serialize.SerializableUtils;
import kafka.utils.VerifiableProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractKafkaBatchConsumer implements IConsumerBatchHandler<String> {
    protected Logger logger = LogManager.getLogger(this.getClass());

    protected VerifiableProperties props;
    protected Consumer<String> consumer;

    public AbstractKafkaBatchConsumer(VerifiableProperties props) {
        super();
        this.props = props;
    }

    /**
     * ObjectOutputStream建立后第一次写入一个对象时， 会在对象数据前写入一些标志的数据“ACED0005”
     *
     * @param messageBytes
     * @return
     */
    @Override
    public String fromBytes(byte[] messageBytes) {
        try {
            // 序列化处理时，要求前4个字节是(byte)0x00AC, (byte)0x00ED, (byte)0x0000, (byte)0x0005
            // 如果不是，则会抛出java.io.StreamCorruptedException异常
            if (hasHead(messageBytes)) {
                Serializable message = (Serializable) SerializableUtils.getObjectFromBytes(messageBytes);
                if (message instanceof String) {
                    return (String) message;
                } else {
                    return JsonUtil.toJsonNonNull(message);
                }
            } else {
                return new String(messageBytes);
            }
        } catch (Exception e) {
            logger.error("Serialize data failed.", e);
            return new String(messageBytes);
        }
    }

    private boolean hasHead(byte[] messageBytes) {
        byte[] head = new byte[]{(byte) 0x00AC, (byte) 0x00ED, (byte) 0x0000, (byte) 0x0005};
        if (messageBytes.length < head.length) return false;
        for (int i = 0; i < head.length; i++) {
            if (messageBytes[i] != head[i]) return false;
        }
        return true;
    }

    @Override
    public void handle(String key, String value) {
        List<KeyValuePair<String>> messages = new ArrayList<>();
        messages.add(new KeyValuePair<>(key, value));
        batchHandle(messages);
    }

    protected void commit() {
        if (this.consumer != null) {
            this.consumer.commit();
        }
    }

    public Consumer<String> getConsumer() {
        return this.consumer;
    }

    public void setConsumer(Consumer<String> consumer) {
        this.consumer = consumer;
    }
}
