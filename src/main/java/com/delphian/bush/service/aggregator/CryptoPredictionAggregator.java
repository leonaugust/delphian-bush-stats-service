package com.delphian.bush.service.aggregator;

import com.delphian.bush.dto.stats.CurrencyStats;
import com.delphian.bush.dto.exchange_rates.ExchangeRate;
import com.delphian.bush.dto.news.CryptoNews;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.streams.kstream.Aggregator;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CryptoPredictionAggregator implements Aggregator<String, String, CurrencyStats> {

    private final ObjectMapper objectMapper;
    public CryptoPredictionAggregator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private static final String NEWS = "news";
    private static final String RATES = "exchange-rates";

    private static final String CURRENCY = "currency";

    @SneakyThrows
    @Override
    public CurrencyStats apply(String key, String value, CurrencyStats aggregate) {
        if (value.contains("kind") && value.contains("domain")) { // Define if it's CryptoNews class by checking if it has fields unique to CryptoNews
            CryptoNews cryptoNews = objectMapper.readValue(value, CryptoNews.class);
            aggregate.addNews(cryptoNews);
            log.error("CryptoNews was added {}", cryptoNews.getSlug());
        } else {
            ExchangeRate exchangeRate = objectMapper.readValue(value, ExchangeRate.class);
            aggregate.addRates(exchangeRate);
            log.error("ExchangeRate was added {}", exchangeRate.getAssetIdQuote());
        }

        aggregate.setCurrency(key.split("-")[0]);
        return aggregate;
    }

}
