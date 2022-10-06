package com.delphian.bush;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.core.CleanupConfig;

@Configuration
public class TestConfig {

	@Bean
	StreamsBuilderFactoryBeanConfigurer config() {
		return fb -> {
			fb.setCleanupConfig(new CleanupConfig(true, true));
		};
	}

}