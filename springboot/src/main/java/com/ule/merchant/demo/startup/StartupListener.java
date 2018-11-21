package com.ule.merchant.demo.startup;

import com.ule.tools.client.kafka.consumer.Consumer;
import com.ule.tools.client.kafka.core.config.ConsumerCfg;
import com.ule.tools.client.kafka.core.config.KafkaClientsConfig;
import kafka.utils.VerifiableProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map.Entry;

public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {
	private static Logger logger = LogManager.getLogger();
	private static VerifiableProperties verifiableProps = initVerifiableProperties();
	public static Long startTime;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent ctxtRfsEvent) {
		logger.info("onApplicationEvent invoked, my parent is :{}", ctxtRfsEvent.getApplicationContext().getParent());
		// 启动kafka接收监听
		// startKafkaConsumers();
        startTime = System.currentTimeMillis();
	}

	private static VerifiableProperties initVerifiableProperties() {
		VerifiableProperties verifiableProps = new VerifiableProperties();
		verifiableProps.props().put("rebalance.max.retries", "5");
		verifiableProps.props().put("rebalance.backoff.ms", "1200");
		return verifiableProps;
	}

	private void startKafkaConsumers() {
		logger.info("starting kafka consumers...");
		int total = 0, succeed = 0;
		try {
			System.setProperty("zookeeper.sasl.client", "false");

			KafkaClientsConfig config = KafkaClientsConfig.DEFAULT_INSTANCE;
			if (config != null) {
				config.reloadConsumers();
				if (config.getConsumers() != null) {
					total = config.getConsumers().size();
					for (Entry<String, ConsumerCfg<?>> cs : config.getConsumers().entrySet()) {
						try {
							Consumer<?> consumer = config.newConsumer(cs.getValue().getId(), verifiableProps);
							// 地址trim一下，防止xml文件自动换行导致连不上服务器(0.5.0以后的版本不需要)
                            consumer.setZookeeperConnect(consumer.getZookeeperConnect().trim());
                            consumer.reload();
							// 参数为启动时间,单位为Seconds,如果<0，则一直开下去，直到结束
							consumer.startReceive(-1);
							succeed++;
						} catch (Throwable t) {
							logger.error("create consumer failed, id:{}", cs.getValue().getId(), t);
						}
					}
				}
			}
		} catch (Throwable t) {
			logger.error("start kafka consumer failed.", t);
		}
		logger.info("kafka consumers started, total:{}, succeed:{}", total, succeed);
	}

}
