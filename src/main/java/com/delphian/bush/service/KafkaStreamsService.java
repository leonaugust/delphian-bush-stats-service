package com.delphian.bush.service;

import org.apache.kafka.streams.StreamsBuilder;

public interface KafkaStreamsService {

    @SuppressWarnings("unused")
    void process(StreamsBuilder streamsBuilder);

}
