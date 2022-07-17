package com.delphian.bush.util.converter;

import com.delphian.bush.dto.news.Currency;
import com.delphian.bush.util.schema.CurrencySchema;
import org.apache.kafka.connect.data.Struct;

public class CurrencyConverter implements ConnectPOJOConverter<Currency> {
    public static final CurrencyConverter INSTANCE = new CurrencyConverter();

    @Override
    public Currency fromConnectData(Struct s) {
        // simple conversion, but more complex types could throw errors
        Currency currency = new Currency();
        currency.setCode(s.getString(CurrencySchema.CODE_FIELD));
        currency.setTitle(s.getString(CurrencySchema.TITLE_FIELD));
        currency.setUrl(s.getString(CurrencySchema.URL_FIELD));
        currency.setSlug(s.getString(CurrencySchema.SLUG_FIELD));
        return currency;
    }
}