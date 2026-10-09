package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.request.BindQuoteRequest;
import com.intellisure.quotepolicyservice.dto.response.CoverageCheckResponse;
import com.intellisure.quotepolicyservice.dto.response.PolicyResponse;
import com.intellisure.quotepolicyservice.dto.response.PolicyStatusResponse;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.PolicyCoverage;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.QuoteCoverage;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import com.intellisure.quotepolicyservice.mapper.PolicyMapper;
import com.intellisure.quotepolicyservice.repository.PolicyCoverageRepository;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.repository.QuoteCoverageRepository;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final QuoteRepository quoteRepository;
    private final QuoteCoverageRepository quoteCoverageRepository;
    private final PolicyRepository policyRepository;
    private final PolicyCoverageRepository policyCoverageRepository;
    private final R2dbcEntityTemplate entityTemplate;
    private final PolicyMapper policyMapper;
    private final SecurityActorService securityActorService;

    @PreAuthorize("hasRole('UNDERWRITER')")
    @Transactional
    public Mono<PolicyResponse> bindQuote(
            UUID quoteId
    ) {
        return policyRepository
                .existsByQuoteId(quoteId)
                .flatMap(policyAlreadyExists -> {
                    if (policyAlreadyExists) {
                        return Mono.error(
                                new BusinessException(
                                        "A policy has already been "
                                                + "created for this quote"
                                )
                        );
                    }

                    return quoteRepository
                            .findById(quoteId)
                            .switchIfEmpty(Mono.error(
                                    new ResourceNotFoundException(
                                            "Quote not found with ID: "
                                                    + quoteId
                                    )
                            ));
                })
                .flatMap(quote ->
                        verifyBindingUnderwriter(quote)
                                .thenReturn(quote)
                )
                .flatMap(quote ->
                        validateQuoteForBinding(quote)
                                .thenReturn(quote)
                )
                .flatMap(quote ->
                        securityActorService
                                .currentUserId()
                                .flatMap(boundByUserId ->
                                        quoteCoverageRepository
                                                .findAllByQuoteId(
                                                        quoteId
                                                )
                                                .collectList()
                                                .flatMap(coverages ->
                                                        createPolicy(
                                                                quote,
                                                                coverages,
                                                                boundByUserId
                                                        )
                                                )
                                )
                );
    }




    @Transactional
    public Mono<Long> activateScheduledPolicies() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        return policyRepository
                .findAllByStatusAndStartDateLessThanEqual(
                        PolicyStatus.PENDING_ISSUANCE,
                        today
                )
                .concatMap(policy -> {
                    policy.setStatus(PolicyStatus.IN_FORCE);
                    policy.setUpdatedAt(now);

                    return entityTemplate.update(policy);
                })
                .count();
    }

    @Transactional
    public Mono<Long> expirePolicies() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        return policyRepository
                .findAllByStatusAndEndDateBefore(
                        PolicyStatus.IN_FORCE,
                        today
                )
                .concatMap(policy -> {
                    policy.setStatus(PolicyStatus.EXPIRED);
                    policy.setExpiredAt(now);
                    policy.setUpdatedAt(now);

                    return entityTemplate.update(policy);
                })
                .count();
    }



    public Mono<CoverageCheckResponse> checkCoverage(
            String policyNumber,
            String coverageCode,
            LocalDate requestedDate
    ) {
        String normalizedCoverageCode =
                coverageCode.trim().toUpperCase();

        return policyRepository
                .findByPolicyNumber(policyNumber)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(
                                "Policy not found with number: "
                                        + policyNumber
                        )
                ))
                .flatMap(policy -> {
                    boolean policyEffective =
                            isDateWithinPolicyPeriod(
                                    policy,
                                    requestedDate
                            );

                    if (!policyEffective) {
                        return Mono.just(
                                coverageNotAvailableResponse(
                                        policy,
                                        normalizedCoverageCode,
                                        requestedDate,
                                        false,
                                        "The policy was not effective "
                                                + "on the requested date"
                                )
                        );
                    }

                    return policyCoverageRepository
                            .findByPolicyIdAndCoverageCode(
                                    policy.getPolicyId(),
                                    normalizedCoverageCode
                            )
                            .filter(coverage ->
                                    isDateWithinCoveragePeriod(
                                            coverage,
                                            requestedDate
                                    )
                            )
                            .map(coverage ->
                                    coverageAvailableResponse(
                                            policy,
                                            coverage,
                                            requestedDate
                                    )
                            )
                            .switchIfEmpty(
                                    Mono.just(
                                            coverageNotAvailableResponse(
                                                    policy,
                                                    normalizedCoverageCode,
                                                    requestedDate,
                                                    true,
                                                    "The requested coverage "
                                                            + "was not present "
                                                            + "or effective on "
                                                            + "the requested date"
                                            )
                                    )
                            );
                });
    }




    public Mono<PolicyStatusResponse> checkPolicyStatus(
            String policyNumber,
            LocalDate requestedDate
    ) {
        return policyRepository
                .findByPolicyNumber(policyNumber)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(
                                "Policy not found with number: "
                                        + policyNumber
                        )
                ))
                .map(policy -> {
                    boolean effectiveOnDate =
                            isDateWithinPolicyPeriod(
                                    policy,
                                    requestedDate
                            );

                    return new PolicyStatusResponse(
                            policy.getPolicyId(),
                            policy.getPolicyNumber(),
                            policy.getCustomerId(),
                            policy.getStatus(),
                            policy.getStartDate(),
                            policy.getEndDate(),
                            effectiveOnDate,
                            requestedDate
                    );
                });
    }

    public Mono<PolicyResponse> getPolicyById(
            UUID policyId
    ) {
        return policyRepository.findById(policyId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(
                                "Policy not found with ID: "
                                        + policyId
                        )
                ))
                .flatMap(policy -> securityActorService
                        .assertCustomerAccess(policy.getCustomerId())
                        .then(Mono.defer(() -> buildPolicyResponse(policy))));
    }

    public Mono<PolicyResponse> getPolicyByNumber(
            String policyNumber
    ) {
        return policyRepository
                .findByPolicyNumber(policyNumber)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(
                                "Policy not found with number: "
                                        + policyNumber
                        )
                ))
                .flatMap(policy -> securityActorService
                        .assertCustomerAccess(policy.getCustomerId())
                        .then(Mono.defer(() -> buildPolicyResponse(policy))));
    }

    public Flux<PolicyResponse> getPoliciesByCustomerId(
            UUID customerId
    ) {
        return securityActorService
                .assertCustomerAccess(customerId)
                .thenMany(policyRepository
                        .findAllByCustomerId(customerId)
                        .flatMap(this::buildPolicyResponse));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMINISTRATOR')")
    public Flux<PolicyResponse> getAllPoliciesForAdministration() {
        return policyRepository.findAll().flatMap(this::buildPolicyResponse);
    }



    private boolean isDateWithinCoveragePeriod(
            PolicyCoverage coverage,
            LocalDate requestedDate
    ) {
        return !requestedDate.isBefore(
                coverage.getEffectiveFrom()
        ) && !requestedDate.isAfter(
                coverage.getEffectiveTo()
        );
    }



    private CoverageCheckResponse coverageAvailableResponse(
            Policy policy,
            PolicyCoverage coverage,
            LocalDate requestedDate
    ) {
        return new CoverageCheckResponse(
                policy.getPolicyId(),
                policy.getPolicyNumber(),
                policy.getCustomerId(),
                policy.getStatus(),
                requestedDate,
                true,
                coverage.getCoverageCode(),
                true,
                coverage.getLimitAmount(),
                coverage.getDeductibleAmount(),
                coverage.getConditions(),
                coverage.getExclusions(),
                coverage.getWaitingPeriodDays(),
                coverage.getEffectiveFrom(),
                coverage.getEffectiveTo(),
                "Coverage was present and effective "
                        + "on the requested date"
        );
    }



    private CoverageCheckResponse coverageNotAvailableResponse(
            Policy policy,
            String coverageCode,
            LocalDate requestedDate,
            boolean policyEffective,
            String message
    ) {
        return new CoverageCheckResponse(
                policy.getPolicyId(),
                policy.getPolicyNumber(),
                policy.getCustomerId(),
                policy.getStatus(),
                requestedDate,
                policyEffective,
                coverageCode,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                message
        );
    }


    private Mono<Void> verifyBindingUnderwriter(
            Quote quote
    ) {
        return securityActorService
                .currentUserId()
                .flatMap(authenticatedUserId -> {
                    if (!authenticatedUserId.equals(
                            quote.getAssignedUnderwriterId()
                    )) {
                        return Mono.error(
                                new AccessDeniedBusinessException(
                                        "Only the assigned underwriter "
                                                + "can bind this quote"
                                )
                        );
                    }

                    return Mono.empty();
                });
    }

    private Mono<Void> validateQuoteForBinding(
            Quote quote
    ) {
        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            return Mono.error(
                    new BusinessException(
                            "Only an ACCEPTED quote can be bound. "
                                    + "Current status: "
                                    + quote.getStatus()
                    )
            );
        }

        if (quote.getQuoteExpiresAt() == null
                || !quote.getQuoteExpiresAt()
                .isAfter(LocalDateTime.now())) {
            return Mono.error(
                    new BusinessException(
                            "The quote has expired and cannot be bound"
                    )
            );
        }

        if (quote.getAcceptedAt() == null
                || quote.getAcceptedByUserId() == null) {
            return Mono.error(
                    new BusinessException(
                            "Customer acceptance information is missing"
                    )
            );
        }

        if (quote.getTotalPremium() == null) {
            return Mono.error(
                    new BusinessException(
                            "The quote does not have a final premium"
                    )
            );
        }

        return Mono.empty();
    }

    private Mono<PolicyResponse> createPolicy(
            Quote quote,
            List<QuoteCoverage> quoteCoverages,
            UUID boundByUserId
    ) {
        if (quoteCoverages.isEmpty()) {
            return Mono.error(
                    new BusinessException(
                            "The quote does not contain coverages"
                    )
            );
        }

        boolean incompleteTerms = quoteCoverages.stream()
                .anyMatch(coverage ->
                        coverage.getOfferedLimit() == null
                                || coverage
                                .getOfferedDeductible() == null
                                || coverage
                                .getCoveragePremium() == null
                );

        if (incompleteTerms) {
            return Mono.error(
                    new BusinessException(
                            "All quote coverages must have final "
                                    + "offered terms before binding"
                    )
            );
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate startDate =
                quote.getRequestedEffectiveDate();
        LocalDate endDate =
                startDate.plusYears(1).minusDays(1);

        UUID policyId = UUID.randomUUID();

        Policy policy = Policy.builder()
                .policyId(policyId)
                .policyNumber(generatePolicyNumber())
                .quoteId(quote.getQuoteId())
                .customerId(quote.getCustomerId())
                .productCode(quote.getProductCode())
                .status(determineInitialPolicyStatus(startDate))
                .startDate(startDate)
                .endDate(endDate)
                .totalPremium(quote.getTotalPremium())
                .issuedByUserId(boundByUserId)
                .boundAt(now)
                .issuedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        List<PolicyCoverage> policyCoverages =
                quoteCoverages.stream()
                        .map(quoteCoverage ->
                                copyCoverage(
                                        policyId,
                                        startDate,
                                        endDate,
                                        quoteCoverage,
                                        now
                                )
                        )
                        .toList();

        return entityTemplate
                .insert(Policy.class)
                .using(policy)
                .flatMap(insertedPolicy ->
                        Flux.fromIterable(policyCoverages)
                                .concatMap(coverage ->
                                        entityTemplate
                                                .insert(
                                                        PolicyCoverage.class
                                                )
                                                .using(coverage)
                                )
                                .collectList()
                                .flatMap(insertedCoverages -> {
                                    quote.setStatus(
                                            QuoteStatus.BOUND
                                    );
                                    quote.setBoundByUserId(
                                            boundByUserId
                                    );
                                    quote.setBoundAt(now);
                                    quote.setUpdatedAt(now);

                                    return entityTemplate
                                            .update(quote)
                                            .thenReturn(
                                                    policyMapper.toResponse(
                                                            insertedPolicy,
                                                            insertedCoverages
                                                    )
                                            );
                                })
                );
    }

    private boolean isDateWithinPolicyPeriod(
            Policy policy,
            LocalDate requestedDate
    ) {
        return !requestedDate.isBefore(
                policy.getStartDate()
        ) && !requestedDate.isAfter(
                policy.getEndDate()
        );
    }


    private PolicyCoverage copyCoverage(
            UUID policyId,
            LocalDate startDate,
            LocalDate endDate,
            QuoteCoverage quoteCoverage,
            LocalDateTime now
    ) {
        return PolicyCoverage.builder()
                .policyCoverageId(UUID.randomUUID())
                .policyId(policyId)
                .coverageCode(
                        quoteCoverage.getCoverageCode()
                )
                .coverageName(
                        quoteCoverage.getCoverageName()
                )
                .limitAmount(
                        quoteCoverage.getOfferedLimit()
                )
                .deductibleAmount(
                        quoteCoverage.getOfferedDeductible()
                )
                .coveragePremium(
                        quoteCoverage.getCoveragePremium()
                )
                .conditions(
                        quoteCoverage.getConditions()
                )
                .exclusions(
                        quoteCoverage.getExclusions()
                )
                .waitingPeriodDays(
                        quoteCoverage.getWaitingPeriodDays()
                )
                .effectiveFrom(startDate)
                .effectiveTo(endDate)
                .createdAt(now)
                .build();
    }



    private PolicyStatus determineInitialPolicyStatus(
            LocalDate startDate
    ) {
        LocalDate today = LocalDate.now();

        if (startDate.isAfter(today)) {
            return PolicyStatus.PENDING_ISSUANCE;
        }

        return PolicyStatus.IN_FORCE;
    }

    private Mono<PolicyResponse> buildPolicyResponse(
            Policy policy
    ) {
        return policyCoverageRepository
                .findAllByPolicyId(policy.getPolicyId())
                .collectList()
                .map(coverages ->
                        policyMapper.toResponse(
                                policy,
                                coverages
                        )
                );
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'ADMIN')")
    @Transactional
    public Mono<PolicyResponse> issuePolicy(UUID policyId) {
        return policyRepository.findById(policyId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Policy not found with ID: " + policyId)))
                .flatMap(policy -> {
                    if (policy.getStatus() != PolicyStatus.PENDING_ISSUANCE && policy.getStatus() != PolicyStatus.BOUND) {
                        return Mono.error(new BusinessException(
                                "Only a PENDING_ISSUANCE or BOUND policy can be issued. Current status: " + policy.getStatus()));
                    }
                    LocalDateTime now = LocalDateTime.now();
                    policy.setStatus(PolicyStatus.IN_FORCE);
                    policy.setIssuedAt(now);
                    policy.setUpdatedAt(now);
                    return entityTemplate.update(policy)
                            .flatMap(updatedPolicy -> {
                                if (updatedPolicy.getQuoteId() != null) {
                                    return quoteRepository.findById(updatedPolicy.getQuoteId())
                                            .flatMap(quote -> {
                                                quote.setStatus(QuoteStatus.ISSUED);
                                                quote.setUpdatedAt(now);
                                                return entityTemplate.update(quote);
                                            })
                                            .thenReturn(updatedPolicy);
                                }
                                return Mono.just(updatedPolicy);
                            });
                })
                .flatMap(this::buildPolicyResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'ADMIN')")
    @Transactional
    public Mono<PolicyResponse> cancelPolicy(UUID policyId, String cancellationReason) {
        return policyRepository.findById(policyId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Policy not found with ID: " + policyId)))
                .flatMap(policy -> {
                    if (policy.getStatus() == PolicyStatus.CANCELLED) {
                        return Mono.error(new BusinessException("Policy is already CANCELLED"));
                    }
                    if (policy.getStatus() == PolicyStatus.EXPIRED) {
                        return Mono.error(new BusinessException("Cannot cancel an EXPIRED policy"));
                    }
                    LocalDateTime now = LocalDateTime.now();
                    policy.setStatus(PolicyStatus.CANCELLED);
                    policy.setUpdatedAt(now);
                    return entityTemplate.update(policy);
                })
                .flatMap(this::buildPolicyResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'ADMIN')")
    @Transactional
    public Mono<PolicyResponse> reinstatePolicy(UUID policyId, String reinstatementReason) {
        return policyRepository.findById(policyId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Policy not found with ID: " + policyId)))
                .flatMap(policy -> {
                    if (policy.getStatus() != PolicyStatus.CANCELLED && policy.getStatus() != PolicyStatus.CANCEL_PENDING) {
                        return Mono.error(new BusinessException(
                                "Only a CANCELLED or CANCEL_PENDING policy can be reinstated. Current status: " + policy.getStatus()));
                    }
                    LocalDateTime now = LocalDateTime.now();
                    policy.setStatus(PolicyStatus.REINSTATED);
                    policy.setUpdatedAt(now);
                    return entityTemplate.update(policy);
                })
                .flatMap(this::buildPolicyResponse);
    }

    private String generatePolicyNumber() {
        return "POL-"
                + Year.now().getValue()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
