package com.delphian.bush.controller;

import com.delphian.bush.dto.stats.CurrencyStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

@RestController
@RequestMapping(("/stats"))
public class StatsController {

    @Autowired
    private KafkaReceiver<String, String> kafkaReceiver;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getStats() {
        Flux<ReceiverRecord<String, String>> receive = kafkaReceiver.receive();

        return receive.checkpoint("Messages being consumed")
                .log()
                .doOnNext(r -> r.receiverOffset().acknowledge())
                .map(ReceiverRecord::value)
                .checkpoint("Consumed all messages");
    }

}
