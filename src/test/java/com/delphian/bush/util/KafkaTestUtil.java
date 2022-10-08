package com.delphian.bush.util;

import com.delphian.bush.util.sender.KafkaSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KafkaTestUtil {
    @Autowired
    @SuppressWarnings(value = "all")
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @SuppressWarnings(value = "all")
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private final Map<Class<?>, KafkaSender<Object>> senders;

    public KafkaTestUtil(List<KafkaSender<Object>> senders) {
        this.senders = senders.stream()
                .collect(Collectors.toMap(KafkaSender::getType, Function.identity()));
    }

    private Consumer<Object, Object> getConsumer(EmbeddedKafkaBroker embeddedKafkaBroker, String topic) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        Consumer<Object, Object> consumer = new DefaultKafkaConsumerFactory<>(consumerProps)
                .createConsumer();
        consumer.subscribe(Collections.singleton(topic));
        return consumer;
    }

    public ConsumerRecord<Object, Object> receive(String topic) {
        Consumer<Object, Object> consumer = getConsumer(embeddedKafkaBroker, topic);
        ConsumerRecord<Object, Object> singleRecord = KafkaTestUtils.getSingleRecord(consumer, topic);
        consumer.close();
        return singleRecord;
    }

    @SneakyThrows
    public <T> void send(T t) {
        KafkaSender<Object> sender = senders.get(t.getClass());
        if (sender == null) {
            throw new UnsupportedOperationException("Kafka sender is not supported for type: " + t.getClass());
        }

        kafkaTemplate.send(
                sender.getTopic(),
                objectMapper.writeValueAsString(sender.getKeyProperties(t)),
                objectMapper.writeValueAsString(t)
        );
    }

}
