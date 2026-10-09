package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.CompletePremiumAuditRequest;
import com.intellisure.quotepolicyservice.dto.InitiatePremiumAuditRequest;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.PremiumAudit;
import com.intellisure.quotepolicyservice.enums.AuditStatus;
import com.intellisure.quotepolicyservice.enums.AuditType;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.mapper.PremiumAuditMapper;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.repository.PremiumAuditRepository;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PremiumAuditServiceCoverageTest {
    private PremiumAuditRepository auditRepository;
    private PolicyRepository policyRepository;
    private SecurityActorService security;
    private PremiumAuditService service;
    private final UUID actor = TestFixtures.UNDERWRITER_ID;

    @BeforeEach
    void setUp() {
        auditRepository = mock(PremiumAuditRepository.class);
        policyRepository = mock(PolicyRepository.class);
        PremiumAuditMapper mapper = mock(PremiumAuditMapper.class);
        security = mock(SecurityActorService.class);
        service = new PremiumAuditService(auditRepository, policyRepository, mapper, security);
        lenient().when(mapper.toResponse(any())).thenReturn(mock(com.intellisure.quotepolicyservice.dto.PremiumAuditResponse.class));
        lenient().when(auditRepository.save(any(PremiumAudit.class)))
                .thenAnswer(i -> Mono.just(i.getArgument(0)));
        lenient().when(security.currentUserId()).thenReturn(Mono.just(actor));
        lenient().when(security.assertCustomerAccess(any(UUID.class))).thenReturn(Mono.empty());
    }

    private Policy policy(PolicyStatus status) {
        Policy p = TestFixtures.policy(status);
        when(policyRepository.findById(p.getPolicyId())).thenReturn(Mono.just(p));
        return p;
    }

    private PremiumAudit audit(AuditStatus status) {
        return PremiumAudit.builder().auditId(UUID.randomUUID()).policyId(TestFixtures.CUSTOMER_ID)
                .auditNumber("AUD-1").auditType(AuditType.SALES).status(status)
                .estimatedExposure(new BigDecimal("100")).exposureBasis("sales")
                .createdAt(TestFixtures.NOW).updatedAt(TestFixtures.NOW).build();
    }

    @Test
    void initiatesForInForceAndExpiredPolicies() {
        InitiatePremiumAuditRequest request = new InitiatePremiumAuditRequest(AuditType.SALES,
                new BigDecimal("100"), "sales");
        Policy inForce = policy(PolicyStatus.IN_FORCE);
        StepVerifier.create(service.initiateAudit(inForce.getPolicyId(), request))
                .expectNextCount(1).verifyComplete();
        Policy expired = policy(PolicyStatus.EXPIRED);
        StepVerifier.create(service.initiateAudit(expired.getPolicyId(), request))
                .expectNextCount(1).verifyComplete();
        Policy draft = policy(PolicyStatus.PENDING_ISSUANCE);
        StepVerifier.create(service.initiateAudit(draft.getPolicyId(), request))
                .expectError(BusinessException.class).verify();
    }

    @Test
    void completesWithAdditionalReturnAndExactPremium() {
        for (BigDecimal actual : new BigDecimal[]{new BigDecimal("125"), new BigDecimal("75"), new BigDecimal("100")}) {
            PremiumAudit audit = audit(AuditStatus.INITIATED);
            when(auditRepository.findById(audit.getAuditId())).thenReturn(Mono.just(audit));
            CompletePremiumAuditRequest request = new CompletePremiumAuditRequest(actual, actor);
            StepVerifier.create(service.completeAudit(audit.getAuditId(), request))
                    .expectNextCount(1).verifyComplete();
        }
    }

    @Test
    void enforcesActorAndCompletionState() {
        PremiumAudit audit = audit(AuditStatus.IN_PROGRESS);
        when(auditRepository.findById(audit.getAuditId())).thenReturn(Mono.just(audit));
        StepVerifier.create(service.completeAudit(audit.getAuditId(),
                        new CompletePremiumAuditRequest(new BigDecimal("101"), TestFixtures.OTHER_UNDERWRITER_ID)))
                .expectError(AccessDeniedBusinessException.class).verify();
        audit.setStatus(AuditStatus.COMPLETED);
        StepVerifier.create(service.completeAudit(audit.getAuditId(),
                        new CompletePremiumAuditRequest(new BigDecimal("101"), actor)))
                .expectError(BusinessException.class).verify();
    }

    @Test
    void cancelsOpenAuditsButNotCompletedAudits() {
        PremiumAudit audit = audit(AuditStatus.INITIATED);
        when(auditRepository.findById(audit.getAuditId())).thenReturn(Mono.just(audit));
        StepVerifier.create(service.cancelAudit(audit.getAuditId()))
                .expectNextCount(1).verifyComplete();
        audit.setStatus(AuditStatus.RETURN_PREMIUM_DUE);
        StepVerifier.create(service.cancelAudit(audit.getAuditId()))
                .expectError(BusinessException.class).verify();
        audit.setStatus(AuditStatus.ADDITIONAL_PREMIUM_DUE);
        StepVerifier.create(service.cancelAudit(audit.getAuditId()))
                .expectError(BusinessException.class).verify();
        audit.setStatus(AuditStatus.COMPLETED);
        StepVerifier.create(service.cancelAudit(audit.getAuditId()))
                .expectError(BusinessException.class).verify();
        Policy auditPolicy = TestFixtures.policy(PolicyStatus.IN_FORCE);
        auditPolicy.setPolicyId(audit.getPolicyId());
        when(policyRepository.findById(audit.getPolicyId())).thenReturn(Mono.just(auditPolicy));
        when(auditRepository.findAllByPolicyId(audit.getPolicyId())).thenReturn(Flux.just(audit));
        StepVerifier.create(service.getAudits(audit.getPolicyId())).expectNextCount(1).verifyComplete();
    }
}
