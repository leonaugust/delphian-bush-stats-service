package com.delphian.bush.util.converter.impl;

import com.delphian.bush.dto.news.CryptoNews;
import com.delphian.bush.dto.news.Currency;
import com.delphian.bush.util.converter.ConnectPOJOConverter;
import com.delphian.bush.util.schema.CryptoNewsSchema;
import com.delphian.bush.util.schema.CurrencySchema;
import com.delphian.bush.util.schema.NewsSourceSchema;
import org.apache.kafka.connect.data.Struct;

import java.util.List;
import java.util.stream.Collectors;

public class CryptoNewsConverter implements ConnectPOJOConverter<CryptoNews> {
    public static final CryptoNewsConverter INSTANCE = new CryptoNewsConverter();

    @Override
    public CryptoNews fromConnectData(Struct s) {
        List<Struct> currenciesStruct = (List<Struct>) s.get(CurrencySchema.SCHEMA_NAME);
        List<Currency> currencies = currenciesStruct.stream().map(c -> CurrencyConverter.INSTANCE.fromConnectData(c))
                .collect(Collectors.toList());

        CryptoNews cryptoNews = new CryptoNews();
        cryptoNews.setSource(NewsSourceConverter.INSTANCE.fromConnectData(s.getStruct(NewsSourceSchema.SCHEMA_NAME)));
        cryptoNews.setCurrencies(currencies);
        cryptoNews.setKind(s.getString(CryptoNewsSchema.KIND_FIELD));
        cryptoNews.setDomain(s.getString(CryptoNewsSchema.DOMAIN_FIELD));
        cryptoNews.setTitle(s.getString(CryptoNewsSchema.TITLE_FIELD));
        cryptoNews.setPublishedAt(s.getString(CryptoNewsSchema.PUBLISHED_AT_FIELD));
        cryptoNews.setSlug(s.getString(CryptoNewsSchema.SLUG_FIELD));
        cryptoNews.setId(s.getString(CryptoNewsSchema.ID_FIELD));
        cryptoNews.setUrl(s.getString(CryptoNewsSchema.URL_FIELD));
        cryptoNews.setCreatedAt(s.getString(CryptoNewsSchema.CREATED_AT_FIELD));

        return cryptoNews;
    }
}