package com.intellisure.quotepolicyservice.controller;

import com.intellisure.quotepolicyservice.dto.SubjectivityResponse;
import com.intellisure.quotepolicyservice.dto.AddSubjectivityRequest;
import com.intellisure.quotepolicyservice.dto.SatisfySubjectivityRequest;
import com.intellisure.quotepolicyservice.dto.WaiveSubjectivityRequest;
import com.intellisure.quotepolicyservice.service.SubjectivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class SubjectivityController {

    private final SubjectivityService subjectivityService;

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{quoteId}/subjectivities")
    public Mono<ResponseEntity<SubjectivityResponse>> addSubjectivity(
            @PathVariable UUID quoteId,
            @Valid @RequestBody AddSubjectivityRequest request) {
        return subjectivityService.addSubjectivity(quoteId, request)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{quoteId}/subjectivities/{subjectivityId}/satisfy")
    public Mono<ResponseEntity<SubjectivityResponse>> satisfySubjectivity(
            @PathVariable UUID quoteId,
            @PathVariable UUID subjectivityId,
            @Valid @RequestBody SatisfySubjectivityRequest request) {
        return subjectivityService.satisfySubjectivity(subjectivityId, request)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{quoteId}/subjectivities/{subjectivityId}/waive")
    public Mono<ResponseEntity<SubjectivityResponse>> waiveSubjectivity(
            @PathVariable UUID quoteId,
            @PathVariable UUID subjectivityId,
            @Valid @RequestBody WaiveSubjectivityRequest request) {
        return subjectivityService.waiveSubjectivity(subjectivityId, request)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'POLICYHOLDER')")
    @GetMapping("/{quoteId}/subjectivities")
    public Flux<SubjectivityResponse> getSubjectivities(@PathVariable UUID quoteId) {
        return subjectivityService.getSubjectivities(quoteId);
    }
}