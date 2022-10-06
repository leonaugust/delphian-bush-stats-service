package com.delphian.bush.util;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Collections;
import java.util.Map;

public class KafkaTestUtil {

    public static Consumer<Object, Object> getConsumer(EmbeddedKafkaBroker embeddedKafkaBroker, String topic) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        Consumer<Object, Object> consumer = new DefaultKafkaConsumerFactory<Object, Object>(consumerProps)
                .createConsumer();
        consumer.subscribe(Collections.singleton(topic));
        return consumer;
    }

}
