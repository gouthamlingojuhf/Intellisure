package com.intellisure.riskunderwritingservice.controller;

import com.intellisure.riskunderwritingservice.dto.request.CreateUnderwritingDecisionRequest;
import com.intellisure.riskunderwritingservice.dto.response.UnderwritingDecisionResponse;
import com.intellisure.riskunderwritingservice.dto.response.UnderwritingResultResponse;
import com.intellisure.riskunderwritingservice.service.UnderwritingDecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(
        name = "Underwriting Decisions",
        description = "Authoritative underwriting decision APIs"
)
public class UnderwritingDecisionController {

    private final UnderwritingDecisionService service;

    @PostMapping(
            "/risk-assessments/{assessmentId}/decisions"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record underwriting decision")
    public Mono<UnderwritingDecisionResponse> recordDecision(
            @PathVariable UUID assessmentId,

            @Valid @RequestBody
            CreateUnderwritingDecisionRequest request
    ) {
        return service.recordDecision(
                assessmentId,
                request
        );
    }

    @GetMapping(
            "/risk-assessments/{assessmentId}/decisions"
    )
    @Operation(summary = "Get underwriting decision history")
    public Flux<UnderwritingDecisionResponse>
    getDecisionHistory(
            @PathVariable UUID assessmentId
    ) {
        return service.getDecisionHistory(
                assessmentId
        );
    }

    @GetMapping(
            "/risk-assessments/{assessmentId}/decisions/latest"
    )
    @Operation(summary = "Get latest underwriting decision")
    public Mono<UnderwritingDecisionResponse>
    getLatestDecision(
            @PathVariable UUID assessmentId
    ) {
        return service.getLatestDecision(
                assessmentId
        );
    }

    @GetMapping(
            "/quotes/{quoteId}/underwriting-result"
    )
    @Operation(
            summary = "Get authoritative underwriting result by quote"
    )
    public Mono<UnderwritingResultResponse>
    getResultByQuoteId(
            @PathVariable UUID quoteId
    ) {
        return service.getResultByQuoteId(
                quoteId
        );
    }
}