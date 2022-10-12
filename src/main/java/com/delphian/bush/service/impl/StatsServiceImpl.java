package com.delphian.bush.service.impl;

import com.delphian.bush.config.kafka.KafkaProperties;
import com.delphian.bush.dto.stats.CurrencyStats;
import com.delphian.bush.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.receiver.internals.ConsumerFactory;
import reactor.kafka.receiver.internals.DefaultKafkaReceiver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final KafkaProperties kafkaProperties;

    private static final String RANDOM_CLIENT_ID_CONFIG = "client_id_" + UUID.randomUUID();

    private static final String RANDOM_GROUP_ID_CONFIG = "group_id_" + UUID.randomUUID();

    @Override
    public Flux<CurrencyStats> getAll() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, RANDOM_CLIENT_ID_CONFIG);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, RANDOM_GROUP_ID_CONFIG);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        DefaultKafkaReceiver<String, CurrencyStats> kafkaReceiver = new DefaultKafkaReceiver<String, CurrencyStats>(
                ConsumerFactory.INSTANCE, ReceiverOptions.<String, CurrencyStats>create(props)
                .subscription(Collections.singleton(kafkaProperties.getStatsTopic()))
        );

        Flux<ReceiverRecord<String, CurrencyStats>> receive = kafkaReceiver.receive();

        return receive.checkpoint("Messages being consumed")
                .log()
                .doOnNext(r -> r.receiverOffset().acknowledge())
                .map(ReceiverRecord::value)
                .checkpoint("Consumed all messages");
    }
}
