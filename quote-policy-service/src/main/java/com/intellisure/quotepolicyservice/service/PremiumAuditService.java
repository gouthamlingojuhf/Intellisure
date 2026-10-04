package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.PremiumAuditResponse;
import com.intellisure.quotepolicyservice.dto.InitiatePremiumAuditRequest;
import com.intellisure.quotepolicyservice.dto.CompletePremiumAuditRequest;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.PremiumAudit;
import com.intellisure.quotepolicyservice.enums.AuditStatus;
import com.intellisure.quotepolicyservice.enums.AuditType;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import com.intellisure.quotepolicyservice.mapper.PremiumAuditMapper;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.repository.PremiumAuditRepository;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PremiumAuditService {

    private final PremiumAuditRepository auditRepository;
    private final PolicyRepository policyRepository;
    private final PremiumAuditMapper auditMapper;
    private final SecurityActorService securityActorService;

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<PremiumAuditResponse> initiateAudit(
            UUID policyId,
            InitiatePremiumAuditRequest request) {
        return policyRepository.findById(policyId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Policy not found: " + policyId)))
                .flatMap(policy -> validatePolicyForAudit(policy))
                .flatMap(policy -> createAudit(policy, request))
                .flatMap(this::buildAuditResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<PremiumAuditResponse> completeAudit(
            UUID auditId,
            CompletePremiumAuditRequest request) {
        return auditRepository.findById(auditId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Audit not found: " + auditId)))
                .flatMap(audit -> validateAuditForCompletion(audit))
                .flatMap(audit -> securityActorService.currentUserId()
                        .flatMap(currentUserId -> {
                            if (!currentUserId.equals(request.auditedByUserId())) {
                                return Mono.error(new AccessDeniedBusinessException("User mismatch"));
                            }
                            return Mono.just(audit);
                        }))
                .flatMap(audit -> {
                    BigDecimal premiumDelta = request.actualExposure().subtract(audit.getEstimatedExposure());
                    BigDecimal additionalPremium = BigDecimal.ZERO;
                    BigDecimal returnPremium = BigDecimal.ZERO;

                    if (premiumDelta.compareTo(BigDecimal.ZERO) > 0) {
                        additionalPremium = premiumDelta;
                    } else if (premiumDelta.compareTo(BigDecimal.ZERO) < 0) {
                        returnPremium = premiumDelta.abs();
                    }

                    audit.setActualExposure(request.actualExposure());
                    audit.setPremiumDelta(premiumDelta);
                    audit.setAdditionalPremium(additionalPremium);
                    audit.setReturnPremium(returnPremium);
                    audit.setStatus(additionalPremium.compareTo(BigDecimal.ZERO) > 0
                            ? AuditStatus.ADDITIONAL_PREMIUM_DUE
                            : returnPremium.compareTo(BigDecimal.ZERO) > 0
                                    ? AuditStatus.RETURN_PREMIUM_DUE
                                    : AuditStatus.COMPLETED);
                    audit.setAuditedByUserId(request.auditedByUserId());
                    audit.setAuditedAt(LocalDateTime.now());
                    audit.setUpdatedAt(LocalDateTime.now());

                    return auditRepository.save(audit);
                })
                .flatMap(this::buildAuditResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<PremiumAuditResponse> cancelAudit(UUID auditId) {
        return auditRepository.findById(auditId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Audit not found: " + auditId)))
                .flatMap(audit -> {
                    if (audit.getStatus() == AuditStatus.COMPLETED
                            || audit.getStatus() == AuditStatus.ADDITIONAL_PREMIUM_DUE
                            || audit.getStatus() == AuditStatus.RETURN_PREMIUM_DUE) {
                        return Mono.error(new BusinessException("Cannot cancel completed audit"));
                    }
                    audit.setStatus(AuditStatus.CANCELLED);
                    audit.setUpdatedAt(LocalDateTime.now());
                    return auditRepository.save(audit);
                })
                .flatMap(this::buildAuditResponse);
    }

    private Mono<Policy> validatePolicyForAudit(Policy policy) {
        if (policy.getStatus() != PolicyStatus.IN_FORCE && policy.getStatus() != PolicyStatus.EXPIRED) {
            return Mono.error(new BusinessException("Audit can only be performed on IN_FORCE or EXPIRED policies. Current: " + policy.getStatus()));
        }
        return Mono.just(policy);
    }

    private Mono<PremiumAudit> validateAuditForCompletion(PremiumAudit audit) {
        if (audit.getStatus() != AuditStatus.INITIATED && audit.getStatus() != AuditStatus.IN_PROGRESS) {
            return Mono.error(new BusinessException("Audit must be INITIATED or IN_PROGRESS to complete. Current: " + audit.getStatus()));
        }
        return Mono.just(audit);
    }

    private Mono<PremiumAudit> createAudit(Policy policy, InitiatePremiumAuditRequest request) {
        LocalDateTime now = LocalDateTime.now();
        PremiumAudit audit = new PremiumAudit(
                UUID.randomUUID(),
                policy.getPolicyId(),
                generateAuditNumber(),
                request.auditType(),
                AuditStatus.INITIATED,
                request.estimatedExposure(),
                null,
                request.exposureBasis(),
                null, null, null,
                null, null,
                now, now
        );

        return auditRepository.save(audit);
    }

    private Mono<PremiumAuditResponse> buildAuditResponse(PremiumAudit audit) {
        return Mono.just(auditMapper.toResponse(audit));
    }

    private String generateAuditNumber() {
        return "AUD-"
                + Year.now().getValue()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}