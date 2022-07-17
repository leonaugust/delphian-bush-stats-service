package com.delphian.bush.util.json.serdes;

import com.delphian.bush.dto.stats.CurrencyStats;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public final class CustomSerdes {
    private CustomSerdes() {
    }

    public static Serde<CurrencyStats> CryptoPrediction() {
        JsonSerializer<CurrencyStats> serializer = new JsonSerializer<>();
        JsonDeserializer<CurrencyStats> deserializer = new JsonDeserializer<>(CurrencyStats.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
}