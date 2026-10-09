package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.RenewalTransactionResponse;
import com.intellisure.quotepolicyservice.dto.InitiateRenewalRequest;
import com.intellisure.quotepolicyservice.dto.RenewalDecisionRequest;
import com.intellisure.quotepolicyservice.dto.BindRenewalRequest;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.PolicyCoverage;
import com.intellisure.quotepolicyservice.entity.RenewalTransaction;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import com.intellisure.quotepolicyservice.enums.RenewalStatus;
import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import com.intellisure.quotepolicyservice.mapper.RenewalTransactionMapper;
import com.intellisure.quotepolicyservice.repository.PolicyCoverageRepository;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.repository.RenewalTransactionRepository;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RenewalService {

    private final RenewalTransactionRepository renewalRepository;
    private final PolicyRepository policyRepository;
    private final PolicyCoverageRepository policyCoverageRepository;
    private final RenewalTransactionMapper renewalMapper;
    private final SecurityActorService securityActorService;

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<RenewalTransactionResponse> initiateRenewal(
            UUID policyId,
            InitiateRenewalRequest request) {
        return policyRepository.findById(policyId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Policy not found: " + policyId)))
                .flatMap(policy -> validatePolicyForRenewal(policy))
                .flatMap(policy -> securityActorService.currentUserId()
                        .flatMap(userId -> createRenewalTransaction(policy, request, userId)))
                .flatMap(this::buildRenewalResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<RenewalTransactionResponse> decideRenewal(
            UUID renewalId,
            RenewalDecisionRequest request) {
        return renewalRepository.findById(renewalId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Renewal not found: " + renewalId)))
                .flatMap(renewal -> validateRenewalForDecision(renewal))
                .flatMap(renewal -> securityActorService.currentUserId()
                        .flatMap(currentUserId -> {
                            if (!currentUserId.equals(request.decidedByUserId())) {
                                return Mono.error(new AccessDeniedBusinessException("User mismatch"));
                            }
                            return Mono.just(renewal);
                        }))
                .flatMap(renewal -> {
                    RenewalStatus newStatus = RenewalStatus.valueOf(request.status());
                    renewal.setStatus(newStatus);
                    renewal.setDecidedByUserId(request.decidedByUserId());
                    renewal.setDecidedAt(LocalDateTime.now());
                    renewal.setDecisionReason(request.decisionReason());
                    renewal.setUpdatedAt(LocalDateTime.now());
                    return renewalRepository.save(renewal);
                })
                .flatMap(this::buildRenewalResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<RenewalTransactionResponse> bindRenewal(
            UUID renewalId,
            BindRenewalRequest request) {
        return renewalRepository.findById(renewalId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Renewal not found: " + renewalId)))
                .flatMap(renewal -> validateRenewalForDecision(renewal))
                .flatMap(renewal -> securityActorService.currentUserId()
                        .flatMap(currentUserId -> {
                            if (!currentUserId.equals(request.boundByUserId())) {
                                return Mono.error(new AccessDeniedBusinessException("User mismatch"));
                            }
                            return Mono.just(renewal);
                        }))
                .flatMap(renewal -> {
                    renewal.setStatus(RenewalStatus.BOUND);
                    renewal.setBoundByUserId(request.boundByUserId());
                    renewal.setBoundAt(LocalDateTime.now());
                    renewal.setUpdatedAt(LocalDateTime.now());
                    return renewalRepository.save(renewal);
                })
                .flatMap(this::buildRenewalResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<RenewalTransactionResponse> issueRenewal(UUID renewalId) {
        return renewalRepository.findById(renewalId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Renewal not found: " + renewalId)))
                .flatMap(renewal -> {
                    if (renewal.getStatus() != RenewalStatus.BOUND) {
                        return Mono.error(new BusinessException("Renewal must be BOUND before issuing. Current: " + renewal.getStatus()));
                    }
                    renewal.setStatus(RenewalStatus.ISSUED);
                    renewal.setIssuedAt(LocalDateTime.now());
                    renewal.setUpdatedAt(LocalDateTime.now());
                    return renewalRepository.save(renewal);
                })
                .flatMap(this::buildRenewalResponse)
                .flatMap(response -> createRenewalPolicy(response));
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'POLICYHOLDER')")
    public Flux<RenewalTransactionResponse> getRenewals(UUID policyId) {
        return policyRepository.findById(policyId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Policy not found: " + policyId)))
                .flatMap(policy -> securityActorService.assertCustomerAccess(policy.getCustomerId())
                        .thenReturn(policy))
                .flatMapMany(policy -> renewalRepository.findAllByPolicyId(policy.getPolicyId())
                        .map(this::buildRenewalResponse)
                        .flatMap(mono -> mono));
    }

    private Mono<Policy> validatePolicyForRenewal(Policy policy) {
        if (policy.getStatus() != PolicyStatus.IN_FORCE && policy.getStatus() != PolicyStatus.EXPIRED) {
            return Mono.error(new BusinessException("Renewal can only be initiated for IN_FORCE or EXPIRED policies. Current: " + policy.getStatus()));
        }
        return Mono.just(policy);
    }

    private Mono<RenewalTransaction> validateRenewalForDecision(RenewalTransaction renewal) {
        if (renewal.getStatus() != RenewalStatus.UNDER_REVIEW && renewal.getStatus() != RenewalStatus.QUOTED) {
            return Mono.error(new BusinessException("Renewal must be in UNDER_REVIEW or QUOTED status for decision. Current: " + renewal.getStatus()));
        }
        return Mono.just(renewal);
    }

    private Mono<RenewalTransaction> createRenewalTransaction(Policy policy, InitiateRenewalRequest request, UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        RenewalTransaction renewal = new RenewalTransaction(
                UUID.randomUUID(),
                policy.getPolicyId(),
                generateRenewalNumber(),
                RenewalStatus.INITIATED,
                request.proposedStartDate(),
                request.proposedEndDate(),
                request.proposedTotalPremium(),
                request.proposedCoverages().toString(),
                request.subjectivities().toString(),
                null, null, null, null, null, null,
                now, now
        );

        return renewalRepository.save(renewal);
    }

    private Mono<RenewalTransactionResponse> buildRenewalResponse(RenewalTransaction renewal) {
        return Mono.just(renewalMapper.toResponse(renewal));
    }

    private Mono<RenewalTransactionResponse> createRenewalPolicy(RenewalTransactionResponse renewal) {
        return policyRepository.findById(renewal.policyId())
                .flatMap(origPolicy -> {
                    UUID newPolicyId = UUID.randomUUID();
                    LocalDateTime now = LocalDateTime.now();
                    Policy renewalPolicy = Policy.builder()
                            .policyId(newPolicyId)
                            .policyNumber(origPolicy.getPolicyNumber() + "-R01")
                            .quoteId(origPolicy.getQuoteId())
                            .customerId(origPolicy.getCustomerId())
                            .productCode(origPolicy.getProductCode())
                            .status(PolicyStatus.IN_FORCE)
                            .startDate(renewal.proposedStartDate())
                            .endDate(renewal.proposedEndDate())
                            .totalPremium(renewal.proposedTotalPremium())
                            .issuedByUserId(renewal.decidedByUserId())
                            .boundAt(renewal.boundAt() != null ? renewal.boundAt() : now)
                            .issuedAt(now)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    return policyRepository.save(renewalPolicy).thenReturn(renewal);
                })
                .defaultIfEmpty(renewal);
    }

    private String generateRenewalNumber() {
        return "REN-"
                + Year.now().getValue()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
