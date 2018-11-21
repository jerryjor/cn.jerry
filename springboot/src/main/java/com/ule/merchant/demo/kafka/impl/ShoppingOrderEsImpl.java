package com.ule.merchant.demo.kafka.impl;

import com.ule.merchant.demo.kafka.AbstractKafkaBatchConsumer;
import com.ule.tools.client.kafka.consumer.handler.KeyValuePair;
import kafka.utils.VerifiableProperties;

import java.util.List;

public class ShoppingOrderEsImpl extends AbstractKafkaBatchConsumer {

    public ShoppingOrderEsImpl(VerifiableProperties props) {
        super(props);
    }

    @Override
    public void batchHandle(List<KeyValuePair<String>> messages) {
        try {
            for (KeyValuePair<String> message : messages) {
                logger.debug("received new message: {}", message.getValue());
            }
        } catch (Throwable t) {
            logger.error("batchHandle messages failed.", t);
        }
    }

}
