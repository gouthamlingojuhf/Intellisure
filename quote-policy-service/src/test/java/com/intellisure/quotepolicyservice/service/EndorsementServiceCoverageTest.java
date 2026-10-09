package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.EndorsementCoverageRequest;
import com.intellisure.quotepolicyservice.dto.EndorsementResponse;
import com.intellisure.quotepolicyservice.dto.RequestEndorsementRequest;
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
import com.intellisure.quotepolicyservice.mapper.EndorsementMapper;
import com.intellisure.quotepolicyservice.mapper.PolicyMapper;
import com.intellisure.quotepolicyservice.repository.EndorsementCoverageRepository;
import com.intellisure.quotepolicyservice.repository.EndorsementRepository;
import com.intellisure.quotepolicyservice.repository.PolicyCoverageRepository;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import com.intellisure.quotepolicyservice.testsupport.EntityTemplateStubber;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EndorsementServiceCoverageTest {
    private EndorsementRepository endorsementRepository;
    private EndorsementCoverageRepository endorsementCoverageRepository;
    private PolicyRepository policyRepository;
    private PolicyCoverageRepository policyCoverageRepository;
    private R2dbcEntityTemplate entityTemplate;
    private SecurityActorService security;
    private EndorsementService service;
    private final UUID actor = TestFixtures.UNDERWRITER_ID;

    @BeforeEach
    void setUp() {
        endorsementRepository = mock(EndorsementRepository.class);
        endorsementCoverageRepository = mock(EndorsementCoverageRepository.class);
        policyRepository = mock(PolicyRepository.class);
        policyCoverageRepository = mock(PolicyCoverageRepository.class);
        entityTemplate = mock(R2dbcEntityTemplate.class);
        security = mock(SecurityActorService.class);
        EndorsementMapper mapper = mock(EndorsementMapper.class);
        lenient().when(mapper.toResponse(any(), any())).thenReturn(mock(EndorsementResponse.class));
        service = new EndorsementService(endorsementRepository, endorsementCoverageRepository,
                policyRepository, policyCoverageRepository, entityTemplate, mapper,
                new PolicyMapper(), security);
        lenient().when(security.currentUserId()).thenReturn(Mono.just(actor));
        lenient().when(security.assertCustomerAccess(any(UUID.class))).thenReturn(Mono.empty());
        lenient().when(entityTemplate.update(any(Endorsement.class)))
                .thenAnswer(i -> Mono.just(i.getArgument(0)));
        lenient().when(entityTemplate.update(any(PolicyCoverage.class)))
                .thenAnswer(i -> Mono.just(i.getArgument(0)));
        lenient().when(entityTemplate.insert(any(Endorsement.class)))
                .thenAnswer(i -> Mono.just(i.getArgument(0)));
        lenient().when(entityTemplate.insert(any(EndorsementCoverage.class)))
                .thenAnswer(i -> Mono.just(i.getArgument(0)));
        lenient().when(entityTemplate.insert(any(PolicyCoverage.class)))
                .thenAnswer(i -> Mono.just(i.getArgument(0)));
        lenient().when(endorsementCoverageRepository.findAllByEndorsementId(any(UUID.class)))
                .thenReturn(Flux.empty());
    }

    private Policy policy(PolicyStatus status) {
        Policy p = TestFixtures.policy(status);
        when(policyRepository.findById(p.getPolicyId())).thenReturn(Mono.just(p));
        return p;
    }

    private RequestEndorsementRequest request(EndorsementCoverageRequest... coverages) {
        return new RequestEndorsementRequest(EndorsementType.ADD_COVERAGE, "Update cover",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30),
                new BigDecimal("25"), List.of(coverages));
    }

    private Endorsement endorsement(EndorsementStatus status, UUID policyId) {
        return Endorsement.builder().endorsementId(UUID.randomUUID()).policyId(policyId)
                .endorsementNumber("END-1").endorsementType(EndorsementType.ADD_COVERAGE)
                .description("change").premiumDelta(new BigDecimal("10"))
                .status(status).requestedByUserId(actor).effectiveFrom(LocalDate.now())
                .createdAt(TestFixtures.NOW).updatedAt(TestFixtures.NOW).build();
    }

    private EndorsementCoverage coverage(Endorsement endorsement, EndorsementOperation op, String code) {
        return new EndorsementCoverage(UUID.randomUUID(), endorsement.getEndorsementId(), code,
                code + " cover", new BigDecimal("100"), new BigDecimal("10"),
                new BigDecimal("5"), "condition", "exclusion", 7, op, TestFixtures.NOW);
    }

    @Test
    void createsEndorsementAndItsCoveragesForInForcePolicy() {
        Policy p = policy(PolicyStatus.IN_FORCE);
        EndorsementCoverageRequest add = new EndorsementCoverageRequest("FIRE", "Fire",
                new BigDecimal("100"), new BigDecimal("10"), new BigDecimal("5"),
                "condition", "exclusion", 7, "ADD");
        StepVerifier.create(service.requestEndorsement(p.getPolicyId(), request(add)))
                .expectNextCount(1).verifyComplete();
    }

    @Test
    void rejectsInvalidPolicyStatus() {
        Policy p = policy(PolicyStatus.CANCELLED);
        StepVerifier.create(service.requestEndorsement(p.getPolicyId(), request()))
                .expectError(BusinessException.class).verify();
    }

    @Test
    void approvesAndRejectsOnlyAsCurrentActor() {
        Policy p = policy(PolicyStatus.BOUND);
        Endorsement e = endorsement(EndorsementStatus.REQUESTED, p.getPolicyId());
        when(endorsementRepository.findById(e.getEndorsementId())).thenReturn(Mono.just(e));
        StepVerifier.create(service.approveEndorsement(e.getEndorsementId(), actor))
                .expectNextCount(1).verifyComplete();

        e.setStatus(EndorsementStatus.REQUESTED);
        StepVerifier.create(service.rejectEndorsement(e.getEndorsementId(), "not suitable", actor))
                .expectNextCount(1).verifyComplete();

        e.setStatus(EndorsementStatus.REQUESTED);
        StepVerifier.create(service.approveEndorsement(e.getEndorsementId(), TestFixtures.OTHER_UNDERWRITER_ID))
                .expectError(AccessDeniedBusinessException.class).verify();
        StepVerifier.create(service.rejectEndorsement(e.getEndorsementId(), "not suitable", TestFixtures.OTHER_UNDERWRITER_ID))
                .expectError(AccessDeniedBusinessException.class).verify();
    }

    @Test
    void issuesAndAppliesAddRemoveAndModifyOperations() {
        Policy p = policy(PolicyStatus.IN_FORCE);
        Endorsement e = endorsement(EndorsementStatus.APPROVED, p.getPolicyId());
        when(endorsementRepository.findById(e.getEndorsementId())).thenReturn(Mono.just(e));
        when(endorsementCoverageRepository.findAllByEndorsementId(e.getEndorsementId()))
                .thenReturn(Flux.just(coverage(e, EndorsementOperation.ADD, "ADD"),
                        coverage(e, EndorsementOperation.REMOVE, "REMOVE"),
                        coverage(e, EndorsementOperation.MODIFY, "MODIFY")));
        when(policyRepository.findById(p.getPolicyId())).thenReturn(Mono.just(p));
        PolicyCoverage remove = TestFixtures.policyCoverage("REMOVE", LocalDate.now(), LocalDate.now().plusDays(1));
        PolicyCoverage modify = TestFixtures.policyCoverage("MODIFY", LocalDate.now(), LocalDate.now().plusDays(1));
        when(policyCoverageRepository.findByPolicyIdAndCoverageCode(any(UUID.class), any(String.class)))
                .thenAnswer(invocation -> "MODIFY".equals(invocation.getArgument(1, String.class))
                        ? Mono.just(modify) : Mono.just(remove));
        when(entityTemplate.delete(any(PolicyCoverage.class))).thenReturn(Mono.just(remove));

        StepVerifier.create(service.issueEndorsement(e.getEndorsementId()))
                .expectNextCount(1).verifyComplete();
        e.setStatus(EndorsementStatus.APPROVED);
        StepVerifier.create(service.applyEndorsementToPolicy(e.getEndorsementId()))
                .expectNext(e).verifyComplete();
        assertEquals(new BigDecimal("100"), modify.getLimitAmount());

        when(endorsementRepository.findAllByPolicyId(p.getPolicyId())).thenReturn(Flux.just(e));
        StepVerifier.create(service.getEndorsements(p.getPolicyId())).expectNextCount(1).verifyComplete();
    }

    @Test
    void rejectsMissingCoverageOrWrongDecisionState() {
        Policy p = policy(PolicyStatus.BOUND);
        Endorsement e = endorsement(EndorsementStatus.REQUESTED, p.getPolicyId());
        when(endorsementRepository.findById(e.getEndorsementId())).thenReturn(Mono.just(e));
        StepVerifier.create(service.issueEndorsement(e.getEndorsementId()))
                .expectError(BusinessException.class).verify();
        e.setStatus(EndorsementStatus.APPROVED);
        EndorsementCoverage remove = coverage(e, EndorsementOperation.REMOVE, "MISSING");
        when(endorsementCoverageRepository.findAllByEndorsementId(e.getEndorsementId()))
                .thenReturn(Flux.just(remove));
        when(policyRepository.findById(p.getPolicyId())).thenReturn(Mono.just(p));
        when(policyCoverageRepository.findByPolicyIdAndCoverageCode(p.getPolicyId(), "MISSING"))
                .thenReturn(Mono.empty());
        StepVerifier.create(service.applyEndorsementToPolicy(e.getEndorsementId()))
                .expectError(BusinessException.class).verify();
    }
}
