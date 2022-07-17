package com.delphian.bush.util.schema;

import com.delphian.bush.dto.news.NewsSource;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import static com.delphian.bush.util.VersionUtil.FIRST_VERSION;

public class NewsSourceSchema {

    public static final String SCHEMA_NAME = NewsSource.class.getName();

    public static final String TITLE_FIELD = "title";
    public static final String REGION_FIELD = "region";
    public static final String DOMAIN_FIELD = "domain";
    public static final String PATH_FIELD = "path";

    public static final Schema SOURCE_SCHEMA = SchemaBuilder.struct()
            .name(SCHEMA_NAME)
            .field(TITLE_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(REGION_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(DOMAIN_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(PATH_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .version(FIRST_VERSION)
            .optional()
            .build();


}
