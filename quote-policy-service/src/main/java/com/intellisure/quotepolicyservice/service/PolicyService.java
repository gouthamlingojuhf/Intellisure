package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.PolicyResponse;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final QuoteRepository quoteRepository;

    public Flux<PolicyResponse> getPolicies(UUID customerId) {
        return policyRepository.findByCustomerId(customerId).map(this::map);
    }

    public Mono<PolicyResponse> getPolicy(UUID id) {
        return policyRepository.findById(id).map(this::map)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Policy not found: " + id)));
    }

    private PolicyResponse map(Policy p) {
        return new PolicyResponse(p.getPolicyId(), p.getQuoteId(), p.getCustomerId(), p.getPolicyNumber(),
                p.getPolicyStatus(), p.getEffectiveDate(), p.getExpiryDate(), p.getTotalPremium(),
                p.getCreatedAt(), p.getUpdatedAt());
    }

    public Mono<PolicyResponse> issuePolicy(UUID quoteId) {
        return quoteRepository.findById(quoteId)
                .flatMap(quote -> {
                    if (!"ACCEPTED".equals(quote.getQuoteStatus())) {
                        return Mono.error(new IllegalArgumentException("Quote must be ACCEPTED to issue a policy"));
                    }
                    
                    LocalDateTime now = LocalDateTime.now();
                    LocalDate effectiveDate = LocalDate.now();
                    
                    Policy policy = Policy.builder()
                            .policyId(UUID.randomUUID())
                            .quoteId(quoteId)
                            .customerId(quote.getCustomerId())
                            .policyNumber("POL-" + System.currentTimeMillis()) // Simplified generation
                            .policyStatus("ACTIVE")
                            .effectiveDate(effectiveDate)
                            .expiryDate(effectiveDate.plusYears(1))
                            .totalPremium(quote.getEstimatedPremium())
                            .createdAt(now)
                            .updatedAt(now)
                            .isNew(true)
                            .build();
                            
                    // Update quote status
                    quote.setQuoteStatus("BOUND");
                    quote.setUpdatedAt(now);
                    quote.setNew(false);

                    return quoteRepository.save(quote)
                            .then(policyRepository.save(policy))
                            .map(savedPolicy -> new PolicyResponse(
                                    savedPolicy.getPolicyId(),
                                    savedPolicy.getQuoteId(),
                                    savedPolicy.getCustomerId(),
                                    savedPolicy.getPolicyNumber(),
                                    savedPolicy.getPolicyStatus(),
                                    savedPolicy.getEffectiveDate(),
                                    savedPolicy.getExpiryDate(),
                                    savedPolicy.getTotalPremium(),
                                    savedPolicy.getCreatedAt(),
                                    savedPolicy.getUpdatedAt()
                            ));
                });
    }
}
