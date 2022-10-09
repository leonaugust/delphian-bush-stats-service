package com.delphian.bush.service.impl;

import com.delphian.bush.config.kafka.KafkaProperties;
import com.delphian.bush.dto.exchange_rates.ExchangeRate;
import com.delphian.bush.dto.news.CryptoNews;
import com.delphian.bush.dto.stats.CurrencyStats;
import com.delphian.bush.util.CryptoNewsTestUtil;
import com.delphian.bush.util.ExchangeRateTestUtil;
import com.delphian.bush.util.KafkaTestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedKafka(partitions = 1,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092", "port=9092"
        },
        topics = {"stats", "news", "exchange-rates", "exchange-rates-intermediate",
                "news-intermediate", "currency-stats-intermediate"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class KafkaStreamsServiceImplTest {

    public static final String SHIB = "SHIB";
    public static final String BTC = "BTC";
    @Autowired
    private KafkaProperties kafkaProperties;

//  https://github.com/spring-projects/spring-kafka/issues/2291
    @BeforeEach
    public void cleanKStreamsContext(ApplicationContext applicationContext) {
        StreamsBuilderFactoryBean streamsBuilderFactory = applicationContext.getBean(StreamsBuilderFactoryBean.class);
        streamsBuilderFactory.setCleanupConfig(new CleanupConfig(true, true));
    }

    @Autowired
    @SuppressWarnings(value = "all")
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTestUtil kafkaTestUtil;

    @Test
    public void processShibaStatsGeneratedTest() throws JsonProcessingException {
        ExchangeRate rateShib = ExchangeRateTestUtil.mockRate(SHIB);
        kafkaTestUtil.send(rateShib);
        CryptoNews newsShib = CryptoNewsTestUtil.mockNews(SHIB, "Top-crypto-projects-that-are-actually-trying-to-do-better-for-the-world");
        kafkaTestUtil.send(newsShib);

        ConsumerRecord<Object, Object> singleRecord = kafkaTestUtil.receive(kafkaProperties.getStatsTopic());
        CurrencyStats currencyStats = objectMapper.readValue((String) singleRecord.value(), CurrencyStats.class);
        assertEquals(1, currencyStats.getNews().size());
        assertEquals(1, currencyStats.getRates().size());

        assertEquals(SHIB, currencyStats.getCurrency());
        assertEquals(rateShib.getRate(), new ArrayList<>(currencyStats.getRates()).get(0).getRate());
        assertEquals(newsShib.getSlug(), new ArrayList<>(currencyStats.getNews()).get(0).getSlug());
    }

    @Test
    public void processMultipleStatsGeneratedTest() throws JsonProcessingException {
        ExchangeRate rateShib = ExchangeRateTestUtil.mockRate(SHIB);
        kafkaTestUtil.send(rateShib);
        CryptoNews newsShib = CryptoNewsTestUtil.mockNews(SHIB, "Top-crypto-projects-that-are-actually-trying-to-do-better-for-the-world");
        kafkaTestUtil.send(newsShib);

        ExchangeRate rateBtc = ExchangeRateTestUtil.mockRate(BTC);
        kafkaTestUtil.send(rateBtc);
        CryptoNews newsBtc = CryptoNewsTestUtil.mockNews(BTC, "Something about BTC");
        kafkaTestUtil.send(newsBtc);

        Iterable<ConsumerRecord<Object, Object>> consumerRecords = kafkaTestUtil.receiveRecords(kafkaProperties.getStatsTopic());
        Map<String, CurrencyStats> statsByCurrency = new HashMap<>();
        for (ConsumerRecord<Object, Object> consumerRecord : consumerRecords) {
            CurrencyStats currencyStats = objectMapper.readValue((String) consumerRecord.value(), CurrencyStats.class);
            statsByCurrency.put(currencyStats.getCurrency(), currencyStats);
        }

        assertEquals(rateBtc.getRate(), new ArrayList<>(statsByCurrency.get(BTC).getRates()).get(0).getRate());
        assertEquals(rateShib.getRate(), new ArrayList<>(statsByCurrency.get(SHIB).getRates()).get(0).getRate());
        assertEquals(newsBtc.getSlug(), new ArrayList<>(statsByCurrency.get(BTC).getNews()).get(0).getSlug());
        assertEquals(newsShib.getSlug(), new ArrayList<>(statsByCurrency.get(SHIB).getNews()).get(0).getSlug());


        assertEquals(SHIB, statsByCurrency.get(SHIB).getCurrency());
        assertEquals(BTC, statsByCurrency.get(BTC).getCurrency());

        assertEquals(2, statsByCurrency.values().size());
        statsByCurrency.values().forEach(s -> {
            assertEquals(1, s.getNews().size());
            assertEquals(1, s.getRates().size());
        });
    }




}