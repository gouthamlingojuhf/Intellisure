package com.intellisure.riskunderwritingservice.controller;

import com.intellisure.riskunderwritingservice.dto.CreateRiskAssessmentRequest;
import com.intellisure.riskunderwritingservice.dto.RiskAssessmentResponse;
import com.intellisure.riskunderwritingservice.service.RiskAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping({"/api/risks", "/api/risk-assessments"})
@RequiredArgsConstructor
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;

    @PostMapping
    public Mono<RiskAssessmentResponse> createAssessment(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody CreateRiskAssessmentRequest request) {
        
        // In a real app with OAuth2, we would get this from Jwt AuthenticationPrincipal.
        // For scaffolding without full security implementation on this microservice yet, 
        // we use a header or a random UUID if not provided.
        if (userId == null) {
            userId = UUID.randomUUID();
        }
        
        return riskAssessmentService.createAssessment(request, userId);
    }
}
