package cn.jerry.fabric.api.fabric.startup;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupListenerConfig {

	@Bean
	public StartupListener getStartupListener() {
		return new StartupListener();
	}
}
