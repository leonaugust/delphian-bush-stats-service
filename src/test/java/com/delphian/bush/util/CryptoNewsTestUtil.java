package com.delphian.bush.util;

import com.delphian.bush.dto.news.CryptoNews;
import com.delphian.bush.dto.news.Currency;
import com.delphian.bush.dto.news.NewsSource;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.delphian.bush.util.TestConstants.*;

public class CryptoNewsTestUtil {

    private static long idCounter = 1000;

    public static CryptoNews mockNews(String assetIdQuote, String newsTitle) {
        NewsSource newsSource = NewsSource.builder()
                .domain("bitcoinist.com")
                .path(null)
                .region("en")
                .title("Bitcoinist")
                .build();

        Currency currency = Currency.builder()
                .code(assetIdQuote)
                .title(assetIdQuote.toLowerCase())
                .slug(assetIdQuote.toLowerCase())
                .url(String.format("https://cryptopanic.com/news/%s/", assetIdQuote))
                .build();

        String id = String.valueOf(idCounter++);

        String url = String.format("https://cryptopanic.com/news/%s/%s", id, newsTitle);
        return CryptoNews.builder()
                .source(newsSource)
                .currencies(Collections.singletonList(currency))
                .createdAt(LocalDateTime.now().toString())
                .domain("bitcoinist.com")
                .id(id)
                .kind("news")
                .url(url)
                .publishedAt(LocalDateTime.now().toString())
                .slug(newsTitle)
                .title(newsTitle)
                .build();
    }

}
