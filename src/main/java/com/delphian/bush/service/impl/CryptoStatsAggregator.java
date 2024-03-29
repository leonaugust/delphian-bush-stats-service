package com.delphian.bush.service.impl;

import com.delphian.bush.dto.stats.CurrencyStats;
import com.delphian.bush.dto.exchange_rates.ExchangeRate;
import com.delphian.bush.dto.news.CryptoNews;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Aggregator;

@Slf4j
public class CryptoStatsAggregator implements Aggregator<String, String, CurrencyStats> {

    private final ObjectMapper objectMapper;
    public CryptoStatsAggregator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public CurrencyStats apply(String key, String value, CurrencyStats aggregate) {
        if (aggregate.getCurrency() == null) {
            aggregate.setCurrency(key.split("-")[0]);
        }

        if (value.contains("kind") && value.contains("domain")) { // Define which class it is
            CryptoNews cryptoNews = objectMapper.readValue(value, CryptoNews.class);
            aggregate.addNews(cryptoNews);
            log.debug("CryptoNews was added {}", cryptoNews.getSlug());
        } else {
            ExchangeRate exchangeRate = objectMapper.readValue(value, ExchangeRate.class);
            aggregate.addRates(exchangeRate);
            log.debug("ExchangeRate was added {}", exchangeRate.getAssetIdQuote());
        }
        return aggregate;
    }

}
