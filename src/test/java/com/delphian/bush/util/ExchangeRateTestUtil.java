package com.delphian.bush.util;

import com.delphian.bush.dto.exchange_rates.ExchangeRate;

import java.util.concurrent.ThreadLocalRandom;

public class ExchangeRateTestUtil {


    public static ExchangeRate mockRate(String assetIdQuote, String date) {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setRate(String.valueOf(ThreadLocalRandom.current().nextDouble(1, 100)));
        exchangeRate.setAssetIdQuote(assetIdQuote);
        exchangeRate.setTime(date);
        return exchangeRate;
    }

}
