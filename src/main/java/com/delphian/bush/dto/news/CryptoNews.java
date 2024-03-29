package com.delphian.bush.dto.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.control.DeepClone;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CryptoNews implements Serializable {

    private String kind;

    private String domain;

    private NewsSource source;

    private String title;

    @JsonProperty("published_at")
    private String publishedAt;

    private String slug;

    private String id;

    private String url;

    @JsonProperty("created_at")
    private String createdAt;

    private List<Currency> currencies;
}