package com.delphian.bush.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
@Profile("kafka")
public class KafkaAutoCreateConfig {
    private final KafkaProperties kafkaProperties;

    @Bean
    public NewTopic cryptoStats() {
        return TopicBuilder.name(kafkaProperties.getStatsTopic())
                .partitions(kafkaProperties.getPartitions())
                .replicas(kafkaProperties.getReplicas())
                .build();
    }

}
