package com.intellisure.riskunderwritingservice.controller;

import com.intellisure.riskunderwritingservice.dto.request.AssignAssessmentRequest;
import com.intellisure.riskunderwritingservice.dto.request.CompleteRiskScoreRequest;
import com.intellisure.riskunderwritingservice.dto.request.CreateRiskAssessmentRequest;
import com.intellisure.riskunderwritingservice.dto.response.RiskAssessmentResponse;
import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.service.RiskAssessmentService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/risk-assessments")
@RequiredArgsConstructor
@Tag(
        name = "Risk Assessments",
        description = "Risk assessment lifecycle APIs"
)
public class RiskAssessmentController {

    private final RiskAssessmentService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a draft risk assessment")
    public Mono<RiskAssessmentResponse> create(
            @Valid @RequestBody
            CreateRiskAssessmentRequest request
    ) {
        return service.createAssessment(request);
    }

    @PatchMapping("/{assessmentId}/start")
    @Operation(summary = "Start a draft assessment")
    public Mono<RiskAssessmentResponse> start(
            @PathVariable UUID assessmentId
    ) {
        return service.startAssessment(
                assessmentId
        );
    }

    @PatchMapping("/{assessmentId}/assignment")
    @Operation(summary = "Assign underwriter and risk engineer")
    public Mono<RiskAssessmentResponse> assign(
            @PathVariable UUID assessmentId,

            @Valid @RequestBody
            AssignAssessmentRequest request
    ) {
        return service.assignAssessment(
                assessmentId,
                request
        );
    }

    @PatchMapping("/{assessmentId}/submit-review")
    @Operation(summary = "Submit assessment for review")
    public Mono<RiskAssessmentResponse> submitReview(
            @PathVariable UUID assessmentId
    ) {
        return service.submitForReview(
                assessmentId
        );
    }

    @PatchMapping("/{assessmentId}/risk-score")
    @Operation(summary = "Record risk score and band")
    public Mono<RiskAssessmentResponse> completeRiskScore(
            @PathVariable UUID assessmentId,

            @Valid @RequestBody
            CompleteRiskScoreRequest request
    ) {
        return service.completeRiskScore(
                assessmentId,
                request
        );
    }

    @GetMapping("/{assessmentId}")
    @Operation(summary = "Get assessment by ID")
    public Mono<RiskAssessmentResponse> getById(
            @PathVariable UUID assessmentId
    ) {
        return service.getById(
                assessmentId
        );
    }

    @GetMapping("/number/{assessmentNumber}")
    @Operation(summary = "Get assessment by number")
    public Mono<RiskAssessmentResponse> getByNumber(
            @PathVariable String assessmentNumber
    ) {
        return service.getByNumber(
                assessmentNumber
        );
    }

    @GetMapping("/quote/{quoteId}")
    @Operation(summary = "Get assessment by quote ID")
    public Mono<RiskAssessmentResponse> getByQuoteId(
            @PathVariable UUID quoteId
    ) {
        return service.getByQuoteId(
                quoteId
        );
    }

    @GetMapping
    @Operation(summary = "Get assessments by status")
    public Flux<RiskAssessmentResponse> getByStatus(
            @RequestParam RiskAssessmentStatus status
    ) {
        return service.getByStatus(status);
    }

    @GetMapping("/underwriter/{underwriterId}")
    @Operation(summary = "Get assigned underwriter queue")
    public Flux<RiskAssessmentResponse> getUnderwriterQueue(
            @PathVariable UUID underwriterId
    ) {
        return service.getAssignedUnderwriterQueue(
                underwriterId
        );
    }
}