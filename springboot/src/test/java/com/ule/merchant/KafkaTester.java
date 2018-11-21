package com.ule.merchant;

import com.ule.tools.client.kafka.consumer.Consumer;
import com.ule.tools.client.kafka.core.config.KafkaClientsConfig;
import kafka.utils.VerifiableProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;

public class KafkaTester {
    private static Logger logger = LogManager.getLogger();
    private static VerifiableProperties verifiableProps = initVerifiableProperties();

    private static VerifiableProperties initVerifiableProperties() {
        VerifiableProperties verifiableProps = new VerifiableProperties();
        verifiableProps.props().put("rebalance.max.retries", "5");
        verifiableProps.props().put("rebalance.backoff.ms", "1200");
        return verifiableProps;
    }

    public static void main(String[] args) {
        KafkaClientsConfig config = KafkaClientsConfig.DEFAULT_INSTANCE;
        try {
            Consumer<?> consumer = config.newConsumer("merchant.data.calc.receiver.shopping.order.es", verifiableProps);
            consumer.setAutoCommitEnable(false);
            consumer.setGroupID(consumer.getGroupID() + "_test");
            consumer.reload();
            consumer.startReceive(-1);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

}
