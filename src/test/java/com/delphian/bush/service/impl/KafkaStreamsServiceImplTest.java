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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

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

    @Autowired
    private KafkaProperties kafkaProperties;

//  https://github.com/spring-projects/spring-kafka/issues/2291
    @BeforeAll
    public static void cleanKStreamsContext(ApplicationContext applicationContext) {
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
    public void processInformationShibaStatsGeneratedTest() throws JsonProcessingException {
        ExchangeRate exchangeRate = ExchangeRateTestUtil.mockRate("SHIB");
        kafkaTestUtil.send(exchangeRate);

        CryptoNews cryptoNews = CryptoNewsTestUtil.mockNews("SHIB",
                "Top-crypto-projects-that-are-actually-trying-to-do-better-for-the-world");
        kafkaTestUtil.send(cryptoNews);

        ConsumerRecord<Object, Object> singleRecord = kafkaTestUtil.receive(kafkaProperties.getStatsTopic());
        CurrencyStats currencyStats = objectMapper.readValue((String) singleRecord.value(), CurrencyStats.class);
        assertEquals(1, currencyStats.getNews().size());
        assertEquals(1, currencyStats.getRates().size());
    }




}