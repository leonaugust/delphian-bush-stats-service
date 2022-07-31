package com.delphian.bush.service.aggregator;

import com.delphian.bush.config.kafka.KafkaProperties;
import com.delphian.bush.dto.news.CryptoNews;
import com.delphian.bush.dto.news.Currency;
import com.delphian.bush.dto.stats.CurrencyStats;
import com.delphian.bush.util.TimeUtil;
import com.delphian.bush.util.json.serdes.CustomSerdes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile("kafka")
public class KafkaStreamsServiceImpl {
    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;

    @Autowired
    public void processInformation(StreamsBuilder streamsBuilder) {
        streamsBuilder.stream(kafkaProperties.getExchangeRatesTopic(), Consumed.with(Serdes.String(), Serdes.String()))
                .filter((key, val) -> {
                    try {
                        Map<String, String> keyProperties = new ObjectMapper().readValue(key, HashMap.class);
                        return TimeUtil.nowFormatted().getDayOfYear() == LocalDateTime.parse(keyProperties.get("date")).getDayOfYear();
                    } catch (Exception e) {
                        return false;
                    }

                })
                .selectKey((key, val) -> {
                    try {
                        Map<String, String> keyProperties = new ObjectMapper().readValue(key, HashMap.class);
                        return keyProperties.get("asset_id_quote") +
                                "-" + LocalDateTime.parse(keyProperties.get("date")).toLocalDate();
                    } catch (Exception e) {
                        return null;
                    }
                }).to("exchange-rates-intermediate"); // Intermediate topic as workaround for issue https://issues.apache.org/jira/browse/KAFKA-10659


        streamsBuilder.stream(kafkaProperties.getNewsTopic(), Consumed.with(Serdes.String(), Serdes.String()))
                .filter((key, val) -> {
                    try {
                        Map<String, String> keyProperties = new ObjectMapper().readValue(key, HashMap.class);
                        return TimeUtil.nowFormatted().getDayOfYear() == LocalDateTime.parse(keyProperties.get("date")).getDayOfYear();
                    } catch (Exception e) {
                        return false;
                    }

                })
                .flatMapValues(value -> {
                    try {
                        final CryptoNews cryptoNews = objectMapper.readValue(value, CryptoNews.class);
                        List<Currency> currencies = cryptoNews.getCurrencies();
                        if (CollectionUtils.isEmpty(currencies)) {
                            return Collections.emptyList();
                        }

                        return currencies.stream().map(cur -> {
                                    CryptoNews singleCurrencyNews = cryptoNews;
                                    singleCurrencyNews.setCurrencies(List.of(cur));
                                    return singleCurrencyNews;
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
                        Map<String, String> keyProperties = new ObjectMapper().readValue(key, HashMap.class);
                        return cryptoNews.getCurrencies().get(0).getCode() +
                                "-" + LocalDateTime.parse(keyProperties.get("date")).toLocalDate();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .to("news-intermediate"); // Intermediate topic as workaround for issue https://issues.apache.org/jira/browse/KAFKA-10659

        KGroupedStream<String, String> ratesToday = streamsBuilder.stream("exchange-rates-intermediate", Consumed.with(Serdes.String(), Serdes.String()))
                .groupByKey();

        KGroupedStream<String, String> newsToday = streamsBuilder.stream("news-intermediate", Consumed.with(Serdes.String(), Serdes.String()))
                .groupByKey();
//
        Aggregator<String, String, CurrencyStats> predictorAggregator = new CryptoPredictionAggregator(objectMapper);
        KStream<String, CurrencyStats> stats = newsToday.cogroup(predictorAggregator)
                .cogroup(ratesToday, predictorAggregator)
                .aggregate(CurrencyStats::new, Materialized.with(Serdes.String(), CustomSerdes.CryptoPrediction())).toStream();

        stats.print(Printed.toSysOut());
        stats.to("currency-stats-intermediate");

        KTable<String, CurrencyStats> table = streamsBuilder.table("currency-stats-intermediate",
                Materialized.with(Serdes.String(), CustomSerdes.CryptoPrediction()));

        table.toStream().to(kafkaProperties.getStatsTopic());
    }


}
