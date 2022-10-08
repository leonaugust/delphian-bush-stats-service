package com.delphian.bush.util;

import com.delphian.bush.dto.exchange_rates.ExchangeRate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.delphian.bush.util.TestConstants.*;

public class ExchangeRateTestUtil {


    public static ExchangeRate mockRate(String assetIdQuote) {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setRate(String.valueOf(ThreadLocalRandom.current().nextDouble(1, 100)));
        exchangeRate.setAssetIdQuote(assetIdQuote);
        exchangeRate.setTime(LocalDateTime.now().toString());
        return exchangeRate;
    }

}
