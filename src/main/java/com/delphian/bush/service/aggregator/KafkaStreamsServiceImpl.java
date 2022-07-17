package com.delphian.bush.service.aggregator;

import com.delphian.bush.config.kafka.KafkaProperties;
import com.delphian.bush.dto.stats.CurrencyStats;
import com.delphian.bush.util.json.serdes.CustomSerdes;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile("kafka")
public class KafkaStreamsServiceImpl {
    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;

    @Autowired
    public void processInformation(StreamsBuilder streamsBuilder) {
        KStream<String, String> newsString = streamsBuilder.stream(kafkaProperties.getNewsTopic());

        newsString.peek((key, value) -> log.trace("News info key: {}, value: {}", key, value));

        KStream<String, String> ratesString = streamsBuilder.stream(kafkaProperties.getExchangeRatesTopic());

        KGroupedStream<String, String> news = newsString.groupByKey();
        KGroupedStream<String, String> rates = ratesString.groupByKey();

        Aggregator<String, String, CurrencyStats> predictorAggregator = new CryptoPredictionAggregator(objectMapper);
        KStream<String, CurrencyStats> stats = news.cogroup(predictorAggregator)
                .cogroup(rates, predictorAggregator)
                .aggregate(CurrencyStats::new, Materialized.with(Serdes.String(), CustomSerdes.CryptoPrediction())).toStream()
                .filter((key, pred) -> pred.getNews().size() != 0);

        // .selectKey(); The problem with news. Because it might have no associated currencies at all. Or have multiple at once.

        stats.print(Printed.toSysOut());

        stats.to(kafkaProperties.getStatsTopic());

    }


}
