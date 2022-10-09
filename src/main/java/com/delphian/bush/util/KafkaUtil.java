package com.delphian.bush.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.kstream.Predicate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class KafkaUtil {

    @SuppressWarnings("unchecked")
    public static Predicate<String, String> isTodayPredicate() {
        return (key, val) -> {
            try {
                Map<String, String> keyProperties = new ObjectMapper().readValue(key, HashMap.class);
                return TimeUtil.nowFormatted().getDayOfYear() == LocalDateTime.parse(keyProperties.get("date")).getDayOfYear();
            } catch (Exception e) {
                return false;
            }

        };
    }

}
