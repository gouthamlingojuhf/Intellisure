package com.intellisure.riskunderwritingservice.controller;

import com.intellisure.riskunderwritingservice.dto.request.CreateRiskFindingRequest;
import com.intellisure.riskunderwritingservice.dto.request.CreateRiskRecommendationRequest;
import com.intellisure.riskunderwritingservice.dto.request.UpdateRecommendationStatusRequest;
import com.intellisure.riskunderwritingservice.dto.response.RiskFindingResponse;
import com.intellisure.riskunderwritingservice.dto.response.RiskRecommendationResponse;
import com.intellisure.riskunderwritingservice.service.RiskFindingService;
import com.intellisure.riskunderwritingservice.service.RiskRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(
        name = "Risk Findings and Recommendations",
        description = "Risk findings and loss-control recommendations"
)
public class RiskEvidenceController {

    private final RiskFindingService findingService;

    private final RiskRecommendationService
            recommendationService;

    @PostMapping(
            "/risk-assessments/{assessmentId}/findings"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a risk finding")
    public Mono<RiskFindingResponse> createFinding(
            @PathVariable UUID assessmentId,

            @Valid @RequestBody
            CreateRiskFindingRequest request
    ) {
        return findingService.createFinding(
                assessmentId,
                request
        );
    }

    @GetMapping(
            "/risk-assessments/{assessmentId}/findings"
    )
    @Operation(summary = "Get assessment findings")
    public Flux<RiskFindingResponse> getFindings(
            @PathVariable UUID assessmentId
    ) {
        return findingService.getFindings(
                assessmentId
        );
    }

    @PostMapping(
            "/risk-assessments/{assessmentId}/recommendations"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a risk recommendation")
    public Mono<RiskRecommendationResponse>
    createRecommendation(
            @PathVariable UUID assessmentId,

            @Valid @RequestBody
            CreateRiskRecommendationRequest request
    ) {
        return recommendationService
                .createRecommendation(
                        assessmentId,
                        request
                );
    }

    @GetMapping(
            "/risk-assessments/{assessmentId}/recommendations"
    )
    @Operation(summary = "Get assessment recommendations")
    public Flux<RiskRecommendationResponse>
    getRecommendations(
            @PathVariable UUID assessmentId
    ) {
        return recommendationService
                .getRecommendations(
                        assessmentId
                );
    }

    @PatchMapping(
            "/risk-assessments/recommendations/"
                    + "{recommendationId}/status"
    )
    @Operation(summary = "Update recommendation status")
    public Mono<RiskRecommendationResponse>
    updateRecommendationStatus(
            @PathVariable UUID recommendationId,

            @Valid @RequestBody
            UpdateRecommendationStatusRequest request
    ) {
        return recommendationService.updateStatus(
                recommendationId,
                request
        );
    }
}