package com.delphian.bush.service.impl;

import com.delphian.bush.config.kafka.KafkaProperties;
import com.delphian.bush.dto.news.CryptoNews;
import com.delphian.bush.dto.news.Currency;
import com.delphian.bush.dto.stats.CurrencyStats;
import com.delphian.bush.service.KafkaStreamsService;
import com.delphian.bush.util.json.serdes.CustomSerdes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.delphian.bush.util.KafkaUtil.isTodayPredicate;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaStreamsServiceImpl implements KafkaStreamsService {
    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;

    @Autowired
    public void process(StreamsBuilder streamsBuilder) {
        filterRatesTodayAndSelectKeyPushToIntermediateTopic(streamsBuilder);
        filterNewsTodayAndSelectKeyPushToIntermediateTopic(streamsBuilder);

        aggregateRatesAndNews(streamsBuilder);
        KTable<String, CurrencyStats> table = streamsBuilder.table(kafkaProperties.getCurrencyStatsIntermediateTopic(),
                Materialized.with(Serdes.String(), CustomSerdes.CryptoPrediction()));

        table.toStream().to(kafkaProperties.getStatsTopic());
    }

    private void aggregateRatesAndNews(StreamsBuilder streamsBuilder) {
        KGroupedStream<String, String> ratesToday = streamsBuilder.stream(kafkaProperties.getExchangeRatesIntermediateTopic(), Consumed.with(Serdes.String(), Serdes.String()))
                .groupByKey();

        KGroupedStream<String, String> newsToday = streamsBuilder.stream(kafkaProperties.getNewsIntermediateTopic(), Consumed.with(Serdes.String(), Serdes.String()))
                .groupByKey();
        Aggregator<String, String, CurrencyStats> predictorAggregator = new CryptoStatsAggregator(objectMapper);
        newsToday.cogroup(predictorAggregator)
                .cogroup(ratesToday, predictorAggregator)
                .aggregate(CurrencyStats::new, Materialized.with(Serdes.String(), CustomSerdes.CryptoPrediction())).toStream()
        .to(kafkaProperties.getCurrencyStatsIntermediateTopic());
    }

    @SuppressWarnings("unchecked")
    private void filterNewsTodayAndSelectKeyPushToIntermediateTopic(StreamsBuilder streamsBuilder) {

        streamsBuilder.stream(kafkaProperties.getNewsTopic(), Consumed.with(Serdes.String(), Serdes.String()))
                .filter(isTodayPredicate())
                .flatMapValues(value -> {
                    try {
                        final CryptoNews cryptoNews = objectMapper.readValue(value, CryptoNews.class);
                        List<Currency> currencies = cryptoNews.getCurrencies();
                        if (CollectionUtils.isEmpty(currencies)) {
                            return Collections.emptyList();
                        }

                        return currencies.stream().map(cur -> {
                                    cryptoNews.setCurrencies(List.of(cur));
                                    return cryptoNews;
                                }).map(news -> {
                                    try {
                                        return new ObjectMapper().writeValueAsString(news);
                                    } catch (JsonProcessingException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .collect(Collectors.toList());
                    } catch (Exception e) {
                        return Collections.emptyList();
                    }
                })
                .selectKey((key, val) -> {
                    try {
                        final CryptoNews cryptoNews = objectMapper.readValue(val, CryptoNews.class);
                        Map<String, String> keyProperties = objectMapper.readValue(key, HashMap.class);
                        return cryptoNews.getCurrencies().get(0).getCode() +
                                "-" + LocalDateTime.parse(keyProperties.get("date")).toLocalDate();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .to(kafkaProperties.getNewsIntermediateTopic()); // Intermediate topic as workaround for issue https://issues.apache.org/jira/browse/KAFKA-10659
    }

    @SuppressWarnings("unchecked")
    private void filterRatesTodayAndSelectKeyPushToIntermediateTopic(StreamsBuilder streamsBuilder) {
        streamsBuilder.stream(kafkaProperties.getExchangeRatesTopic(), Consumed.with(Serdes.String(), Serdes.String()))
                .filter(isTodayPredicate())
                .selectKey((key, val) -> {
                    try {
                        Map<String, String> keyProperties = objectMapper.readValue(key, HashMap.class);
                        return keyProperties.get("asset_id_quote") +
                                "-" + LocalDateTime.parse(keyProperties.get("date")).toLocalDate();
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }).to(kafkaProperties.getExchangeRatesIntermediateTopic()); // Intermediate topic as workaround for issue https://issues.apache.org/jira/browse/KAFKA-10659
    }


}
