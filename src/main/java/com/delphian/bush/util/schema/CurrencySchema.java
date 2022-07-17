package com.delphian.bush.util.schema;

import com.delphian.bush.dto.news.Currency;
import com.delphian.bush.util.converter.ConnectPOJOConverter;
import com.delphian.bush.util.converter.CurrencyConverter;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

public class CurrencySchema {


    public static final String SCHEMA_NAME = Currency.class.getName();

    public static final String CODE_FIELD = "code";
    public static final String TITLE_FIELD = "title";
    public static final String SLUG_FIELD = "slug";
    public static final String URL_FIELD = "url";

    public static final Schema CURRENCY_SCHEMA = SchemaBuilder.struct()
            .name(SCHEMA_NAME)
            .doc("A currency item")
            .field(CODE_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(TITLE_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(SLUG_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(URL_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .optional()
            .build();

    public static final ConnectPOJOConverter<Currency> CONVERTER = new CurrencyConverter();





}
