package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.BindRenewalRequest;
import com.intellisure.quotepolicyservice.dto.InitiateRenewalRequest;
import com.intellisure.quotepolicyservice.dto.RenewalDecisionRequest;
import com.intellisure.quotepolicyservice.dto.RenewalTransactionResponse;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.RenewalTransaction;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import com.intellisure.quotepolicyservice.enums.RenewalStatus;
import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.mapper.RenewalTransactionMapper;
import com.intellisure.quotepolicyservice.repository.PolicyCoverageRepository;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.repository.RenewalTransactionRepository;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RenewalServiceCoverageTest {
    private RenewalTransactionRepository renewalRepository;
    private PolicyRepository policyRepository;
    private SecurityActorService security;
    private RenewalTransactionMapper mapper;
    private RenewalService service;
    private final UUID actor = TestFixtures.UNDERWRITER_ID;

    @BeforeEach
    void setUp() {
        renewalRepository = mock(RenewalTransactionRepository.class);
        policyRepository = mock(PolicyRepository.class);
        PolicyCoverageRepository coverageRepository = mock(PolicyCoverageRepository.class);
        mapper = mock(RenewalTransactionMapper.class);
        security = mock(SecurityActorService.class);
        service = new RenewalService(renewalRepository, policyRepository, coverageRepository, mapper, security);
        lenient().when(mapper.toResponse(any())).thenAnswer(i -> response(i.getArgument(0)));
        lenient().when(security.currentUserId()).thenReturn(Mono.just(actor));
        lenient().when(security.assertCustomerAccess(any(UUID.class))).thenReturn(Mono.empty());
        lenient().when(renewalRepository.save(any(RenewalTransaction.class)))
                .thenAnswer(i -> Mono.just(i.getArgument(0)));
        lenient().when(policyRepository.save(any(Policy.class)))
                .thenAnswer(i -> Mono.just(i.getArgument(0)));
    }

    private Policy policy(PolicyStatus status) {
        Policy p = TestFixtures.policy(status);
        when(policyRepository.findById(p.getPolicyId())).thenReturn(Mono.just(p));
        return p;
    }

    private RenewalTransaction renewal(RenewalStatus status, UUID policyId) {
        return RenewalTransaction.builder().renewalId(UUID.randomUUID()).policyId(policyId)
                .renewalNumber("REN-1").status(status).proposedStartDate(LocalDate.now())
                .proposedEndDate(LocalDate.now().plusYears(1)).proposedTotalPremium(new BigDecimal("100"))
                .proposedCoverageSnapshot("[]").subjectivities("[]")
                .createdAt(TestFixtures.NOW).updatedAt(TestFixtures.NOW).build();
    }

    private RenewalTransactionResponse response(RenewalTransaction r) {
        return new RenewalTransactionResponse(r.getRenewalId(), r.getPolicyId(), r.getRenewalNumber(),
                r.getStatus(), r.getProposedStartDate(), r.getProposedEndDate(), r.getProposedTotalPremium(),
                List.of(), List.of(), r.getDecidedByUserId(), r.getDecidedAt(), r.getDecisionReason(),
                r.getBoundByUserId(), r.getBoundAt(), r.getIssuedAt(), r.getCreatedAt(), r.getUpdatedAt());
    }

    private InitiateRenewalRequest request() {
        return new InitiateRenewalRequest(LocalDate.now(), LocalDate.now().plusYears(1),
                new BigDecimal("100"), List.of(), List.of());
    }

    @Test
    void initiatesForInForceAndExpiredPolicies() {
        Policy inForce = policy(PolicyStatus.IN_FORCE);
        StepVerifier.create(service.initiateRenewal(inForce.getPolicyId(), request()))
                .expectNextCount(1).verifyComplete();
        Policy expired = policy(PolicyStatus.EXPIRED);
        StepVerifier.create(service.initiateRenewal(expired.getPolicyId(), request()))
                .expectNextCount(1).verifyComplete();
        Policy draft = policy(PolicyStatus.PENDING_ISSUANCE);
        StepVerifier.create(service.initiateRenewal(draft.getPolicyId(), request()))
                .expectError(BusinessException.class).verify();
    }

    @Test
    void decidesBindsAndIssuesRenewalPolicy() {
        Policy p = policy(PolicyStatus.IN_FORCE);
        RenewalTransaction renewal = renewal(RenewalStatus.UNDER_REVIEW, p.getPolicyId());
        when(renewalRepository.findById(renewal.getRenewalId())).thenReturn(Mono.just(renewal));
        StepVerifier.create(service.decideRenewal(renewal.getRenewalId(),
                        new RenewalDecisionRequest("QUOTED", "approved", actor)))
                .expectNextCount(1).verifyComplete();
        renewal.setStatus(RenewalStatus.QUOTED);
        StepVerifier.create(service.bindRenewal(renewal.getRenewalId(), new BindRenewalRequest(actor)))
                .expectNextCount(1).verifyComplete();
        when(policyRepository.findById(p.getPolicyId())).thenReturn(Mono.just(p));
        StepVerifier.create(service.issueRenewal(renewal.getRenewalId())).expectNextCount(1).verifyComplete();
        when(renewalRepository.findAllByPolicyId(p.getPolicyId())).thenReturn(Flux.just(renewal));
        StepVerifier.create(service.getRenewals(p.getPolicyId())).expectNextCount(1).verifyComplete();
    }

    @Test
    void rejectsActorInvalidDecisionAndIssueState() {
        Policy p = policy(PolicyStatus.IN_FORCE);
        RenewalTransaction renewal = renewal(RenewalStatus.UNDER_REVIEW, p.getPolicyId());
        when(renewalRepository.findById(renewal.getRenewalId())).thenReturn(Mono.just(renewal));
        StepVerifier.create(service.decideRenewal(renewal.getRenewalId(),
                        new RenewalDecisionRequest("QUOTED", "", TestFixtures.OTHER_UNDERWRITER_ID)))
                .expectError(AccessDeniedBusinessException.class).verify();
        StepVerifier.create(service.bindRenewal(renewal.getRenewalId(),
                        new BindRenewalRequest(TestFixtures.OTHER_UNDERWRITER_ID)))
                .expectError(AccessDeniedBusinessException.class).verify();
        renewal.setStatus(RenewalStatus.INITIATED);
        StepVerifier.create(service.bindRenewal(renewal.getRenewalId(), new BindRenewalRequest(actor)))
                .expectError(BusinessException.class).verify();
        StepVerifier.create(service.issueRenewal(renewal.getRenewalId())).expectError(BusinessException.class).verify();
    }
}
