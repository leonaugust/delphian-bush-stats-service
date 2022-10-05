package com.delphian.bush.util.converter.impl;

import com.delphian.bush.dto.news.NewsSource;
import com.delphian.bush.util.converter.ConnectPOJOConverter;
import com.delphian.bush.util.schema.NewsSourceSchema;
import org.apache.kafka.connect.data.Struct;

public class NewsSourceConverter implements ConnectPOJOConverter<NewsSource> {
    public static final NewsSourceConverter INSTANCE = new NewsSourceConverter();

    @Override
    public NewsSource fromConnectData(Struct s) {
        // simple conversion, but more complex types could throw errors
        NewsSource newsSource = new NewsSource();
        newsSource.setTitle(s.getString(NewsSourceSchema.TITLE_FIELD));
        newsSource.setRegion(s.getString(NewsSourceSchema.REGION_FIELD));
        newsSource.setDomain(s.getString(NewsSourceSchema.DOMAIN_FIELD));
        newsSource.setPath(s.getString(NewsSourceSchema.PATH_FIELD));
        return newsSource;
    }
}