package com.delphian.bush.controller;

import com.delphian.bush.dto.stats.CurrencyStats;
import com.delphian.bush.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping(("/stats"))
public class StatsController {

    private final StatsService statsService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CurrencyStats> getAll() {
        return statsService.getAll();
    }

}
