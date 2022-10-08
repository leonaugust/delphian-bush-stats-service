package com.delphian.bush.util.sender.impl;

import com.delphian.bush.config.kafka.KafkaProperties;
import com.delphian.bush.dto.news.CryptoNews;
import com.delphian.bush.util.TimeUtil;
import com.delphian.bush.util.sender.KafkaSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.delphian.bush.util.TestConstants.*;
import static com.delphian.bush.util.TestConstants.DATE_FIELD;

@Service
@RequiredArgsConstructor
public class CryptoNewsSenderImpl<T> implements KafkaSender<CryptoNews> {

    private final KafkaProperties kafkaProperties;

    @Override
    public Map<String, String> getKeyProperties(CryptoNews cryptoNews) {
        Map<String, String> newsProperties = new HashMap<>();
        newsProperties.put(APPLICATION_CONFIG, APPLICATION);
        newsProperties.put(ID_FIELD, cryptoNews.getId());
        newsProperties.put(DATE_FIELD, TimeUtil.nowFormatted().toString());
        return newsProperties;
    }

    @Override
    public String getTopic() {
        return kafkaProperties.getNewsTopic();
    }

    @Override
    public Class<CryptoNews> getType() {
        return CryptoNews.class;
    }
}
