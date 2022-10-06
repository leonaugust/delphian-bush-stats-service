package com.delphian.bush.service.impl;

import com.delphian.bush.config.kafka.KafkaProperties;
import com.delphian.bush.dto.exchange_rates.ExchangeRate;
import com.delphian.bush.dto.news.CryptoNews;
import com.delphian.bush.dto.news.Currency;
import com.delphian.bush.dto.news.NewsSource;
import com.delphian.bush.dto.stats.CurrencyStats;
import com.delphian.bush.util.KafkaTestUtil;
import com.delphian.bush.util.TimeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.Collections;
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
        },
        controlledShutdown = false
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class KafkaStreamsServiceImplTest {

    public static final String APPLICATION = "application";
    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @SuppressWarnings(value = "all")
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @SuppressWarnings(value = "all")
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    public static final String APPLICATION_CONFIG = "application";
    public static final String TIME_FIELD = "time";
    public static final String ASSET_ID_QUOTE_FIELD = "asset_id_quote";

    public static final String DATE_FIELD = "date";

    private static final String ID_FIELD = "id";

    @Test
    public void processInformationTest() throws JsonProcessingException, InterruptedException {
        StreamsBuilderFactoryBean streamsBuilderFactory = applicationContext.getBean(StreamsBuilderFactoryBean.class);
        streamsBuilderFactory.setCleanupConfig(new CleanupConfig(true, true));

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setRate("10");
        exchangeRate.setAssetIdQuote("SHIB");
        exchangeRate.setTime(LocalDateTime.now().toString());

        Map<String, String> exchangeRateProps = new HashMap<>();
        exchangeRateProps.put(APPLICATION_CONFIG, APPLICATION);
        exchangeRateProps.put(ASSET_ID_QUOTE_FIELD, exchangeRate.getAssetIdQuote());
        exchangeRateProps.put(TIME_FIELD, exchangeRate.getTime());
        exchangeRateProps.put(DATE_FIELD, TimeUtil.nowFormatted().toString());

        kafkaTemplate.send(
                kafkaProperties.getExchangeRatesTopic(),
                objectMapper.writeValueAsString(exchangeRateProps),
                objectMapper.writeValueAsString(exchangeRate)
        );

        NewsSource newsSource = NewsSource.builder()
                .domain("bitcoinist.com")
                .path(null)
                .region("en")
                .title("Bitcoinist")
                .build();

        Currency currency = Currency.builder()
                .code("SHIB")
                .title("Shiba Inu")
                .slug("shiba-inu")
                .url("https://cryptopanic.com/news/shiba-inu/")
                .build();

        CryptoNews cryptoNews = CryptoNews.builder()
                .source(newsSource)
                .currencies(Collections.singletonList(currency))
                .createdAt(LocalDateTime.now().toString())
                .domain("bitcoinist.com")
                .id("15482265")
                .kind("news")
                .url("https://cryptopanic.com/news/15482265/Top-crypto-projects-that-are-actually-trying-to-do-better-for-the-world")
                .publishedAt(LocalDateTime.now().toString())
                .slug("Top-crypto-projects-that-are-actually-trying-to-do-better-for-the-world")
                .title("Top crypto projects that are actually trying to do better for the world")
                .build();

        Map<String, String> newsProperties = new HashMap<>();
        newsProperties.put(APPLICATION_CONFIG, APPLICATION);
        newsProperties.put(ID_FIELD, cryptoNews.getId());
        newsProperties.put(DATE_FIELD, TimeUtil.nowFormatted().toString());

        kafkaTemplate.send(kafkaProperties.getNewsTopic(),
                objectMapper.writeValueAsString(newsProperties),
                objectMapper.writeValueAsString(cryptoNews)
        );

        Consumer<Object, Object> consumer = KafkaTestUtil.getConsumer(embeddedKafkaBroker, kafkaProperties.getStatsTopic());
        ConsumerRecord<Object, Object> singleRecord = KafkaTestUtils.getSingleRecord(consumer, kafkaProperties.getStatsTopic());
        CurrencyStats currencyStats = objectMapper.readValue((String) singleRecord.value(), CurrencyStats.class);
        consumer.close();
        assertEquals(1, currencyStats.getNews().size());
        assertEquals(1, currencyStats.getRates().size());
    }


}