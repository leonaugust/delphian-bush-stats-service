package com.delphian.bush.dto.exchange_rates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateResponse {

    @JsonProperty("asset_id_base")
    private String assetIdBase;

    private List<ExchangeRate> rates;
}
