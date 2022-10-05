package com.delphian.bush.service;

import com.delphian.bush.dto.stats.CurrencyStats;
import reactor.core.publisher.Flux;

public interface StatsService {

    Flux<CurrencyStats> getAll();

}
