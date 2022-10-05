package com.delphian.bush.service.impl;

import com.delphian.bush.dto.stats.CurrencyStats;
import com.delphian.bush.service.StatsService;
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
public class StatsServiceImpl implements StatsService {

    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";

    private static final String TOPIC = "stats";

    private static final String CLIENT_ID_CONFIG = "client_id_" + UUID.randomUUID();

    private static final String GROUP_ID_CONFIG = "group_id_" + UUID.randomUUID();

    @Override
    public Flux<CurrencyStats> getAll() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID_CONFIG);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        DefaultKafkaReceiver kafkaReceiver = new DefaultKafkaReceiver(
                ConsumerFactory.INSTANCE, ReceiverOptions.create(props)
                .subscription(Collections.singleton(TOPIC))
        );

        Flux<ReceiverRecord<String, CurrencyStats>> receive = kafkaReceiver.receive(200);

        return receive.checkpoint("Messages being consumed")
                .log()
                .doOnNext(r -> r.receiverOffset().acknowledge())
                .map(ReceiverRecord::value)
                .checkpoint("Consumed all messages");
    }
}
