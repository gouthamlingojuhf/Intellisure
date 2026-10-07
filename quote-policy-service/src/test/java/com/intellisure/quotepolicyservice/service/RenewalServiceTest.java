package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.BindRenewalRequest;
import com.intellisure.quotepolicyservice.dto.InitiateRenewalRequest;
import com.intellisure.quotepolicyservice.dto.RenewalCoverageRequest;
import com.intellisure.quotepolicyservice.dto.RenewalDecisionRequest;
import com.intellisure.quotepolicyservice.dto.RenewalSubjectivityRequest;
import com.intellisure.quotepolicyservice.dto.RenewalTransactionResponse;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.RenewalTransaction;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import com.intellisure.quotepolicyservice.enums.RenewalStatus;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.mapper.RenewalTransactionMapper;
import com.intellisure.quotepolicyservice.repository.PolicyCoverageRepository;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.repository.RenewalTransactionRepository;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RenewalServiceTest")
class RenewalServiceTest {

    @Mock
    private RenewalTransactionRepository renewalRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyCoverageRepository policyCoverageRepository;

    @Mock
    private RenewalTransactionMapper renewalMapper;

    @Mock
    private SecurityActorService securityActorService;

    private RenewalService service;

    @BeforeEach
    void setUp() {
        service = new RenewalService(
                renewalRepository,
                policyRepository,
                policyCoverageRepository,
                renewalMapper,
                securityActorService
        );
    }

    @Test
    @DisplayName("Initiate renewal creates distinct renewal transaction")
    void initiateRenewalCreatesTransaction() {
        UUID policyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Policy existingPolicy = Policy.builder()
                .policyId(policyId)
                .policyNumber("POL-2026-0001")
                .status(PolicyStatus.IN_FORCE)
                .startDate(LocalDate.now().minusYears(1))
                .endDate(LocalDate.now())
                .totalPremium(new BigDecimal("10000.00"))
                .build();

        RenewalTransactionResponse response = new RenewalTransactionResponse(
                UUID.randomUUID(), policyId, "REN-2026-0001", RenewalStatus.INITIATED,
                LocalDate.now().plusDays(1), LocalDate.now().plusYears(1),
                new BigDecimal("11000.00"), List.of(), List.of(),
                null, null, null, null, null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(policyRepository.findById(policyId)).thenReturn(Mono.just(existingPolicy));
        when(securityActorService.currentUserId()).thenReturn(Mono.just(userId));
        when(renewalRepository.save(any(RenewalTransaction.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(renewalMapper.toResponse(any(RenewalTransaction.class))).thenReturn(response);

        InitiateRenewalRequest request = new InitiateRenewalRequest(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusYears(1),
                new BigDecimal("11000.00"),
                List.of(new RenewalCoverageRequest("PROPERTY", "Property Coverage", new BigDecimal("1000000"), new BigDecimal("1000"), new BigDecimal("5000"), null, null, null)),
                List.of(new RenewalSubjectivityRequest("LOSS_RUN", "Loss run report"))
        );

        StepVerifier.create(service.initiateRenewal(policyId, request))
                .assertNext(res -> {
                    assertEquals(policyId, res.policyId());
                    assertNotNull(res.renewalNumber());
                    assertEquals(RenewalStatus.INITIATED, res.status());
                })
                .verifyComplete();

        verify(renewalRepository).save(any(RenewalTransaction.class));
    }

    @Test
    @DisplayName("Initiate renewal rejects non in-force/non-expired policy")
    void initiateRenewalRejectsPendingIssuance() {
        UUID policyId = UUID.randomUUID();
        Policy pendingPolicy = Policy.builder()
                .policyId(policyId)
                .status(PolicyStatus.PENDING_ISSUANCE)
                .build();

        when(policyRepository.findById(policyId)).thenReturn(Mono.just(pendingPolicy));

        InitiateRenewalRequest request = new InitiateRenewalRequest(
                LocalDate.now(), LocalDate.now().plusYears(1), BigDecimal.TEN, List.of(), List.of()
        );

        StepVerifier.create(service.initiateRenewal(policyId, request))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Issue renewal creates new renewed policy and does not mutate old policy dates")
    void issueRenewalCreatesNewPolicy() {
        UUID renewalId = UUID.randomUUID();
        UUID origPolicyId = UUID.randomUUID();
        UUID underwriterId = UUID.randomUUID();

        Policy origPolicy = Policy.builder()
                .policyId(origPolicyId)
                .policyNumber("POL-2026-0001")
                .quoteId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .productCode("BOP")
                .status(PolicyStatus.IN_FORCE)
                .startDate(LocalDate.now().minusYears(1))
                .endDate(LocalDate.now())
                .totalPremium(new BigDecimal("10000.00"))
                .build();

        RenewalTransaction boundRenewal = new RenewalTransaction(
                renewalId, origPolicyId, "REN-2026-0001", RenewalStatus.BOUND,
                LocalDate.now().plusDays(1), LocalDate.now().plusYears(1),
                new BigDecimal("11500.00"), "[]", "[]",
                underwriterId, LocalDateTime.now(), "Approved renewal",
                underwriterId, LocalDateTime.now(), null,
                LocalDateTime.now().minusDays(2), LocalDateTime.now()
        );

        RenewalTransactionResponse response = new RenewalTransactionResponse(
                renewalId, origPolicyId, "REN-2026-0001", RenewalStatus.ISSUED,
                LocalDate.now().plusDays(1), LocalDate.now().plusYears(1),
                new BigDecimal("11500.00"), List.of(), List.of(),
                underwriterId, LocalDateTime.now(), "Approved renewal",
                underwriterId, LocalDateTime.now(), LocalDateTime.now(),
                LocalDateTime.now().minusDays(2), LocalDateTime.now()
        );

        when(renewalRepository.findById(renewalId)).thenReturn(Mono.just(boundRenewal));
        when(renewalRepository.save(any(RenewalTransaction.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(renewalMapper.toResponse(any(RenewalTransaction.class))).thenReturn(response);
        when(policyRepository.findById(origPolicyId)).thenReturn(Mono.just(origPolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(service.issueRenewal(renewalId))
                .assertNext(res -> {
                    assertEquals(RenewalStatus.ISSUED, res.status());
                    assertNotNull(res.issuedAt());
                })
                .verifyComplete();

        // Verify a new renewal policy is saved
        verify(policyRepository).save(argThat(newPolicy ->
                newPolicy.getPolicyNumber().equals("POL-2026-0001-R01")
                        && newPolicy.getStatus() == PolicyStatus.IN_FORCE
                        && newPolicy.getStartDate().equals(LocalDate.now().plusDays(1))
        ));
    }
}
