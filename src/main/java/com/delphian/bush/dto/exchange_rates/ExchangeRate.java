package com.delphian.bush.dto.exchange_rates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRate {

    private String time;

    @JsonProperty("asset_id_quote")
    private String assetIdQuote;

    private String rate;
}
