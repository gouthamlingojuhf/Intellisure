package com.intellisure.quotepolicyservice.controller;

import com.intellisure.quotepolicyservice.dto.request.BindQuoteRequest;
import com.intellisure.quotepolicyservice.dto.response.PolicyResponse;
import com.intellisure.quotepolicyservice.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.intellisure.quotepolicyservice.dto.response.CoverageCheckResponse;
import com.intellisure.quotepolicyservice.dto.response.PolicyStatusResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(
        name = "Policy Management",
        description = "Reactive APIs for policy binding and retrieval"
)
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping("/bind/{quoteId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Bind an accepted quote",
            description = """
                    Binds an accepted quote and creates an
                    immutable policy with policy coverages.
                    """
    )
    public Mono<PolicyResponse> bindQuote(
            @PathVariable UUID quoteId
    ) {
        return policyService.bindQuote(quoteId);
    }



    @GetMapping("/number/{policyNumber}/status")
    @Operation(
            summary = "Check policy status on a date",
            description = """
                Used by Claims to determine whether the policy
                period covered the requested loss date.
                """
    )
    public Mono<PolicyStatusResponse> checkPolicyStatus(
            @PathVariable String policyNumber,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate requestedDate
    ) {
        return policyService.checkPolicyStatus(
                policyNumber,
                requestedDate
        );
    }

    @GetMapping("/{policyId}")
    @Operation(summary = "Get policy by ID")
    @PreAuthorize("""
                    hasAnyRole(
                    'POLICYHOLDER',
                    'UNDERWRITER',
                    'ADMIN',
                    'CLAIMS_ADJUSTER',
                    'CLAIMS_MANAGER',
                    'CLAIMS_SERVICE'
                    )
                    """)
    public Mono<PolicyResponse> getPolicyById(
            @PathVariable UUID policyId
    ) {
        return policyService.getPolicyById(policyId);
    }

    @GetMapping("/number/{policyNumber}")
    @Operation(summary = "Get policy by policy number")
    @PreAuthorize("""
                    hasAnyRole(
                    'POLICYHOLDER',
                    'UNDERWRITER',
                    'ADMIN',
                    'CLAIMS_ADJUSTER',
                    'CLAIMS_MANAGER',
                    'CLAIMS_SERVICE'
                    )
                    """)
    public Mono<PolicyResponse> getPolicyByNumber(
            @PathVariable String policyNumber
    ) {
        return policyService.getPolicyByNumber(
                policyNumber
        );
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer policies")
    @PreAuthorize("hasAnyRole('POLICYHOLDER', 'ADMIN')")
    public Flux<PolicyResponse> getPoliciesByCustomerId(
            @PathVariable UUID customerId
    ) {
        return policyService.getPoliciesByCustomerId(
                customerId
        );
    }



    @GetMapping("/number/{policyNumber}/coverage-check")
    @Operation(
            summary = "Check policy coverage on a date",
            description = """
                Returns authoritative policy coverage information
                for a requested coverage code and loss date.
                """
    )
    @PreAuthorize("""
                    hasAnyRole(
                    'CLAIMS_ADJUSTER',
                    'CLAIMS_MANAGER',
                    'CLAIMS_SERVICE',
                    'UNDERWRITER',
                    'ADMIN'
                    )
                    """)
    public Mono<CoverageCheckResponse> checkCoverage(
            @PathVariable String policyNumber,

            @RequestParam String coverageCode,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate requestedDate
    ) {
        return policyService.checkCoverage(
                policyNumber,
                coverageCode,
                requestedDate
        );
    }
}