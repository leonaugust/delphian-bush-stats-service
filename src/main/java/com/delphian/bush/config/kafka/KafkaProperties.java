package com.delphian.bush.config.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties(prefix = "predictor.kafka")
@Getter
@Setter
@Profile("kafka")
public class KafkaProperties {

    private String statsTopic;
    private String newsTopic;
    private String exchangeRatesTopic;
    private int partitions;
    private int replicas;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


}
