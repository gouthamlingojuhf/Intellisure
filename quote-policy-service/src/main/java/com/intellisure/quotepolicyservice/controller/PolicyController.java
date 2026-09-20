package com.intellisure.quotepolicyservice.controller;

import com.intellisure.quotepolicyservice.dto.PolicyResponse;
import com.intellisure.quotepolicyservice.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping("/issue/{quoteId}")
    public Mono<PolicyResponse> issuePolicy(@PathVariable UUID quoteId) {
        return policyService.issuePolicy(quoteId);
    }


    @GetMapping
    public Flux<PolicyResponse> getPolicies(@RequestParam UUID customerId) {
        return policyService.getPolicies(customerId);
    }

    @GetMapping("/{policyId}")
    public Mono<PolicyResponse> getPolicy(@PathVariable UUID policyId) {
        return policyService.getPolicy(policyId);
    }
}
