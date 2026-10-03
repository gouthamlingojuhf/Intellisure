package com.intellisure.analyticsintelligenceservice.controller;

import com.intellisure.analyticsintelligenceservice.dto.ExecutiveDashboardSummaryResponse;
import com.intellisure.analyticsintelligenceservice.dto.GenerateRiskScoreRequest;
import com.intellisure.analyticsintelligenceservice.dto.LossRatioMetricsResponse;
import com.intellisure.analyticsintelligenceservice.dto.LossTriangleListResponse;
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

    @GetMapping("/loss-ratio")
    public Mono<com.intellisure.analyticsintelligenceservice.dto.LossRatioMetricsResponse> getLossRatioMetrics() {
        return analyticsService.getLatestLossRatioMetrics();
    }

    @GetMapping("/loss-triangle/{year}")
    public Mono<com.intellisure.analyticsintelligenceservice.dto.LossTriangleListResponse> getLossTriangle(@PathVariable Integer year) {
        return analyticsService.getLossTriangle(year);
    }

    @GetMapping("/dashboard/summary")
    public Mono<com.intellisure.analyticsintelligenceservice.dto.ExecutiveDashboardSummaryResponse> getDashboardSummary() {
        return analyticsService.getDashboardSummary();
    }
}