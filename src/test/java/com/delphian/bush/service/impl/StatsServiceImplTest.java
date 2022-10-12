package com.delphian.bush.service.impl;

import com.delphian.bush.config.kafka.KafkaProperties;
import com.delphian.bush.dto.exchange_rates.ExchangeRate;
import com.delphian.bush.dto.news.CryptoNews;
import com.delphian.bush.dto.stats.CurrencyStats;
import com.delphian.bush.service.StatsService;
import com.delphian.bush.util.CryptoNewsTestUtil;
import com.delphian.bush.util.ExchangeRateTestUtil;
import com.delphian.bush.util.KafkaTestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.ArrayList;

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
class StatsServiceImplTest {


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

    @Autowired
    private StatsService statsService;

    @Test
    public void getAllTest() throws JsonProcessingException {
        ExchangeRate rateShib = ExchangeRateTestUtil.mockRate(SHIB, LocalDateTime.now().toString());
        kafkaTestUtil.send(rateShib);
        CryptoNews newsShib = CryptoNewsTestUtil.mockNews(SHIB, "Top-crypto-projects-that-are-actually-trying-to-do-better-for-the-world");
        kafkaTestUtil.send(newsShib);

//        List<CurrencyStats> receivedRecords = statsService.getAll()
//                .checkpoint("Received records")
//                .collectList()
//                .subscribe();

//        objectMapper.readValue((String) consumerRecord.value(), CurrencyStats.class)

        Object stats = statsService.getAll()
                .checkpoint("Received records")
                .blockFirst();
        CurrencyStats currencyStats = objectMapper.readValue((String) stats, CurrencyStats.class);
        assertEquals(1, currencyStats.getNews().size());
        assertEquals(1, currencyStats.getRates().size());

        assertEquals(SHIB, currencyStats.getCurrency());
        assertEquals(rateShib.getRate(), new ArrayList<>(currencyStats.getRates()).get(0).getRate());
        assertEquals(newsShib.getSlug(), new ArrayList<>(currencyStats.getNews()).get(0).getSlug());

    }

}