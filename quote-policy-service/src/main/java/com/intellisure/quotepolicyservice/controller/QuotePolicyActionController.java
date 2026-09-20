package com.intellisure.quotepolicyservice.controller;

import com.intellisure.quotepolicyservice.dto.PolicyResponse;
import com.intellisure.quotepolicyservice.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuotePolicyActionController {
    private final PolicyService policyService;

    @PostMapping("/{quoteId}/issue-policy")
    public Mono<PolicyResponse> issuePolicy(@PathVariable UUID quoteId) {
        return policyService.issuePolicy(quoteId);
    }
}
