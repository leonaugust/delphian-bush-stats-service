package com.delphian.bush.dto.stats;

import com.delphian.bush.dto.exchange_rates.ExchangeRate;
import com.delphian.bush.dto.news.CryptoNews;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyStats implements Serializable {

    private String currency;
    private Set<CryptoNews> news = new HashSet<>();
    private Set<ExchangeRate> rates = new HashSet<>();

    public void addNews(CryptoNews story) {
        news.add(story);
    }

    public void addRates(ExchangeRate rate) {
        rates.add(rate);
    }

}
