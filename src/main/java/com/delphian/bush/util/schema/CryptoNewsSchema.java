package com.delphian.bush.util.schema;

import com.delphian.bush.dto.news.CryptoNews;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import static com.delphian.bush.util.VersionUtil.FIRST_VERSION;
import static com.delphian.bush.util.schema.CurrencySchema.CURRENCY_SCHEMA;
import static com.delphian.bush.util.schema.NewsSourceSchema.SOURCE_SCHEMA;

public class CryptoNewsSchema {

    public static final String SCHEMA_NAME = CryptoNews.class.getName();
    public static final String ID_FIELD = "id";
    public static final String KIND_FIELD = "kind";
    public static final String DOMAIN_FIELD = "domain";
    public static final String TITLE_FIELD = "title";
    public static final String PUBLISHED_AT_FIELD = "published_at";
    public static final String SLUG_FIELD = "slug";
    public static final String URL_FIELD = "url";
    public static final String CREATED_AT_FIELD = "created_at";


    public static final Schema NEWS_SCHEMA = SchemaBuilder.struct()
            .name(SCHEMA_NAME)
            .version(FIRST_VERSION)
            .field(NewsSourceSchema.SCHEMA_NAME,  SOURCE_SCHEMA)
            .field(CurrencySchema.SCHEMA_NAME, SchemaBuilder.array(CURRENCY_SCHEMA).optional())
            .field(KIND_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(DOMAIN_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(TITLE_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(PUBLISHED_AT_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(SLUG_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(ID_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(URL_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(CREATED_AT_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .build();

//    public static final Schema NEWS_KEY_SCHEMA = SchemaBuilder.struct()
//            .version(FIRST_VERSION)
//            .field(APPLICATION_CONFIG, Schema.STRING_SCHEMA)
//            .field(ID_FIELD, Schema.STRING_SCHEMA); // also add currency name. Should divide names, because news can have multiple news related to currencies

}
