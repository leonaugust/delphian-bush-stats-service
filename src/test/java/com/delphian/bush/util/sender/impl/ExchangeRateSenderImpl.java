package com.delphian.bush.util.sender.impl;

import com.delphian.bush.config.kafka.KafkaProperties;
import com.delphian.bush.dto.exchange_rates.ExchangeRate;
import com.delphian.bush.util.TimeUtil;
import com.delphian.bush.util.sender.KafkaSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.delphian.bush.util.TestConstants.*;

@Service
@RequiredArgsConstructor
public class ExchangeRateSenderImpl<V> implements KafkaSender<ExchangeRate> {

    private final KafkaProperties kafkaProperties;

    @Override
    public Map<String, String> getKeyProperties(ExchangeRate exchangeRate) {
        Map<String, String> exchangeRateProps = new HashMap<>();
        exchangeRateProps.put(APPLICATION_CONFIG, APPLICATION);
        exchangeRateProps.put(ASSET_ID_QUOTE_FIELD, exchangeRate.getAssetIdQuote());
        exchangeRateProps.put(TIME_FIELD, exchangeRate.getTime());
        exchangeRateProps.put(DATE_FIELD, TimeUtil.nowFormatted().toString());
        return exchangeRateProps;
    }

    @Override
    public String getTopic() {
        return kafkaProperties.getExchangeRatesTopic();
    }

    @Override
    public Class<ExchangeRate> getType() {
        return ExchangeRate.class;
    }
}
