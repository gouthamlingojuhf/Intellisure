package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.EndorsementCoverageResponse;
import com.intellisure.quotepolicyservice.dto.EndorsementResponse;
import com.intellisure.quotepolicyservice.dto.RequestEndorsementRequest;
import com.intellisure.quotepolicyservice.dto.EndorsementCoverageRequest;
import com.intellisure.quotepolicyservice.entity.Endorsement;
import com.intellisure.quotepolicyservice.entity.EndorsementCoverage;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.PolicyCoverage;
import com.intellisure.quotepolicyservice.enums.EndorsementOperation;
import com.intellisure.quotepolicyservice.enums.EndorsementStatus;
import com.intellisure.quotepolicyservice.enums.EndorsementType;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import com.intellisure.quotepolicyservice.mapper.EndorsementMapper;
import com.intellisure.quotepolicyservice.mapper.PolicyMapper;
import com.intellisure.quotepolicyservice.repository.EndorsementCoverageRepository;
import com.intellisure.quotepolicyservice.repository.EndorsementRepository;
import com.intellisure.quotepolicyservice.repository.PolicyCoverageRepository;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
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
public class EndorsementService {

    private final EndorsementRepository endorsementRepository;
    private final EndorsementCoverageRepository endorsementCoverageRepository;
    private final PolicyRepository policyRepository;
    private final PolicyCoverageRepository policyCoverageRepository;
    private final R2dbcEntityTemplate entityTemplate;
    private final EndorsementMapper endorsementMapper;
    private final PolicyMapper policyMapper;
    private final SecurityActorService securityActorService;

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<EndorsementResponse> requestEndorsement(
            UUID policyId,
            RequestEndorsementRequest request) {
        return policyRepository.findById(policyId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Policy not found: " + policyId)))
                .flatMap(policy -> validatePolicyForEndorsement(policy))
                .flatMap(policy -> securityActorService.currentUserId()
                        .flatMap(userId -> createEndorsement(policy, request, userId)))
                .flatMap(this::buildEndorsementResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<EndorsementResponse> approveEndorsement(
            UUID endorsementId,
            UUID approvedByUserId) {
        return endorsementRepository.findById(endorsementId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Endorsement not found: " + endorsementId)))
                .flatMap(endorsement -> validateEndorsementForDecision(endorsement, EndorsementStatus.REQUESTED))
                .flatMap(endorsement -> securityActorService.currentUserId()
                        .flatMap(currentUserId -> {
                            if (!currentUserId.equals(approvedByUserId)) {
                                return Mono.error(new AccessDeniedBusinessException("User mismatch"));
                            }
                            return Mono.just(endorsement);
                        }))
                .flatMap(endorsement -> {
                    endorsement.setStatus(EndorsementStatus.APPROVED);
                    endorsement.setApprovedByUserId(approvedByUserId);
                    endorsement.setApprovedAt(LocalDateTime.now());
                    endorsement.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(endorsement);
                })
                .flatMap(this::buildEndorsementResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<EndorsementResponse> rejectEndorsement(
            UUID endorsementId,
            String decisionReason,
            UUID rejectedByUserId) {
        return endorsementRepository.findById(endorsementId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Endorsement not found: " + endorsementId)))
                .flatMap(endorsement -> validateEndorsementForDecision(endorsement, EndorsementStatus.REQUESTED))
                .flatMap(endorsement -> securityActorService.currentUserId()
                        .flatMap(currentUserId -> {
                            if (!currentUserId.equals(rejectedByUserId)) {
                                return Mono.error(new AccessDeniedBusinessException("User mismatch"));
                            }
                            return Mono.just(endorsement);
                        }))
                .flatMap(endorsement -> {
                    endorsement.setStatus(EndorsementStatus.REJECTED);
                    endorsement.setApprovedByUserId(rejectedByUserId);
                    endorsement.setApprovedAt(LocalDateTime.now());
                    endorsement.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(endorsement);
                })
                .flatMap(this::buildEndorsementResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<EndorsementResponse> issueEndorsement(UUID endorsementId) {
        return endorsementRepository.findById(endorsementId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Endorsement not found: " + endorsementId)))
                .flatMap(endorsement -> validateEndorsementForDecision(endorsement, EndorsementStatus.APPROVED))
                .flatMap(endorsement -> {
                    endorsement.setStatus(EndorsementStatus.ISSUED);
                    endorsement.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(endorsement);
                })
                .flatMap(this::buildEndorsementResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<Endorsement> applyEndorsementToPolicy(UUID endorsementId) {
        return endorsementRepository.findById(endorsementId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Endorsement not found: " + endorsementId)))
                .flatMap(endorsement -> validateEndorsementForDecision(endorsement, EndorsementStatus.APPROVED))
                .flatMap(endorsement -> endorsementCoverageRepository.findAllByEndorsementId(endorsement.getEndorsementId())
                        .collectList()
                        .flatMap(coverages -> policyRepository.findById(endorsement.getPolicyId())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Policy not found: " + endorsement.getPolicyId())))
                                .flatMap(policy -> applyEndorsementCoverages(
                                        policy,
                                        coverages,
                                        endorsement.getEffectiveFrom(),
                                        endorsement.getEffectiveTo())
                                        .thenReturn(endorsement))));
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'POLICYHOLDER')")
    public Flux<EndorsementResponse> getEndorsements(UUID policyId) {
        return policyRepository.findById(policyId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Policy not found: " + policyId)))
                .flatMap(policy -> securityActorService.assertCustomerAccess(policy.getCustomerId())
                        .thenReturn(policy))
                .flatMapMany(policy -> endorsementRepository.findAllByPolicyId(policy.getPolicyId())
                        .flatMap(this::buildEndorsementResponse));
    }

    private Mono<Endorsement> createEndorsement(Policy policy, RequestEndorsementRequest request, UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        Endorsement endorsement = new Endorsement(
                UUID.randomUUID(),
                policy.getPolicyId(),
                generateEndorsementNumber(),
                request.endorsementType(),
                request.description(),
                request.premiumDelta(),
                EndorsementStatus.REQUESTED,
                userId,
                null,
                request.effectiveFrom(),
                request.effectiveTo(),
                now,
                null,
                now,
                now
        );

        return entityTemplate.insert(endorsement)
                .flatMap(saved -> saveEndorsementCoverages(saved, request.coverages()).thenReturn(saved));
    }

    private Mono<Void> saveEndorsementCoverages(Endorsement endorsement, List<EndorsementCoverageRequest> coverages) {
        return Flux.fromIterable(coverages)
                .flatMap(covReq -> {
                    EndorsementCoverage coverage = new EndorsementCoverage(
                            UUID.randomUUID(),
                            endorsement.getEndorsementId(),
                            covReq.coverageCode(),
                            covReq.coverageName(),
                            covReq.limitAmount(),
                            covReq.deductibleAmount(),
                            covReq.coveragePremium(),
                            covReq.conditions(),
                            covReq.exclusions(),
                            covReq.waitingPeriodDays(),
                            EndorsementOperation.valueOf(covReq.operation()),
                            LocalDateTime.now()
                    );
                    return entityTemplate.insert(coverage);
                })
                .then();
    }

    private Mono<EndorsementResponse> buildEndorsementResponse(Endorsement endorsement) {
        return endorsementCoverageRepository.findAllByEndorsementId(endorsement.getEndorsementId())
                .collectList()
                .map(coverages -> endorsementMapper.toResponse(endorsement, coverages));
    }

    private Mono<Policy> validatePolicyForEndorsement(Policy policy) {
        if (policy.getStatus() != PolicyStatus.IN_FORCE && policy.getStatus() != PolicyStatus.BOUND) {
            return Mono.error(new BusinessException("Endorsements can only be requested on IN_FORCE or BOUND policies. Current status: " + policy.getStatus()));
        }
        return Mono.just(policy);
    }

    private Mono<Endorsement> validateEndorsementForDecision(Endorsement endorsement, EndorsementStatus requiredStatus) {
        if (endorsement.getStatus() != requiredStatus) {
            return Mono.error(new BusinessException("Endorsement must be in " + requiredStatus + " status for this action. Current: " + endorsement.getStatus()));
        }
        return Mono.just(endorsement);
    }

    private Mono<Void> applyEndorsementCoverages(Policy policy, List<EndorsementCoverage> endorsementCoverages, LocalDate effectiveFrom, LocalDate effectiveTo) {
        return Flux.fromIterable(endorsementCoverages)
                .flatMap(ec -> {
                    EndorsementOperation op = ec.getOperation();
                    return switch (op) {
                        case ADD -> addCoverage(policy, ec, effectiveFrom, effectiveTo);
                        case REMOVE -> removeCoverage(policy, ec.getCoverageCode());
                        case MODIFY -> modifyCoverage(policy, ec, effectiveFrom, effectiveTo);
                    };
                })
                .then();
    }

    private Mono<Void> addCoverage(Policy policy, EndorsementCoverage ec, LocalDate effectiveFrom, LocalDate effectiveTo) {
        PolicyCoverage newCoverage = new PolicyCoverage(
                UUID.randomUUID(),
                policy.getPolicyId(),
                ec.getCoverageCode(),
                ec.getCoverageName(),
                ec.getLimitAmount(),
                ec.getDeductibleAmount(),
                ec.getCoveragePremium(),
                ec.getConditions(),
                ec.getExclusions(),
                ec.getWaitingPeriodDays(),
                effectiveFrom,
                effectiveTo,
                LocalDateTime.now()
        );
        return entityTemplate.insert(newCoverage).then();
    }

    private Mono<Void> removeCoverage(Policy policy, String coverageCode) {
        return policyCoverageRepository.findByPolicyIdAndCoverageCode(policy.getPolicyId(), coverageCode)
                .switchIfEmpty(Mono.error(new BusinessException("Coverage not found for removal: " + coverageCode)))
                .flatMap(coverage -> entityTemplate.delete(coverage).then());
    }

    private Mono<Void> modifyCoverage(Policy policy, EndorsementCoverage ec, LocalDate effectiveFrom, LocalDate effectiveTo) {
        return policyCoverageRepository.findByPolicyIdAndCoverageCode(policy.getPolicyId(), ec.getCoverageCode())
                .switchIfEmpty(Mono.error(new BusinessException("Coverage not found for modification: " + ec.getCoverageCode())))
                .flatMap(coverage -> {
                    coverage.setLimitAmount(ec.getLimitAmount());
                    coverage.setDeductibleAmount(ec.getDeductibleAmount());
                    coverage.setCoveragePremium(ec.getCoveragePremium());
                    coverage.setConditions(ec.getConditions());
                    coverage.setExclusions(ec.getExclusions());
                    coverage.setWaitingPeriodDays(ec.getWaitingPeriodDays());
                    coverage.setEffectiveFrom(effectiveFrom);
                    coverage.setEffectiveTo(effectiveTo);
                    return entityTemplate.update(coverage).then();
                });
    }

    private String generateEndorsementNumber() {
        return "END-"
                + Year.now().getValue()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
