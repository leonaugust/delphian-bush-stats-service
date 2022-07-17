package com.delphian.bush.dto.news;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsSource {

    private String title;
    private String region;
    private String domain;
    private String path;
}
