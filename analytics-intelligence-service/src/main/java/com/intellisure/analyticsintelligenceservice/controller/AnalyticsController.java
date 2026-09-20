package com.intellisure.analyticsintelligenceservice.controller;

import com.intellisure.analyticsintelligenceservice.dto.GenerateRiskScoreRequest;
import com.intellisure.analyticsintelligenceservice.dto.RiskScoreResponse;
import com.intellisure.analyticsintelligenceservice.service.AnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/risk-score")
    public Mono<RiskScoreResponse> generateRiskScore(@Valid @RequestBody GenerateRiskScoreRequest request) {
        return analyticsService.generateRiskScore(request);
    }
}
