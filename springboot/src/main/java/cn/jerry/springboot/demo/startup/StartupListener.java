package cn.jerry.springboot.demo.startup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {
	private static Logger logger = LogManager.getLogger();
	public static Long startTime;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent ctxtRfsEvent) {
		logger.info("onApplicationEvent invoked, my parent is :{}", ctxtRfsEvent.getApplicationContext().getParent());
		// 启动kafka接收监听
		// startKafkaConsumers();
        startTime = System.currentTimeMillis();
	}

}
