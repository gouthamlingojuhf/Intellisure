package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.client.CustomerPartyAdjusterClient;
import com.intellisure.claimsservice.dto.ClaimResponse;
import com.intellisure.claimsservice.dto.FileClaimRequest;
import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.exception.BusinessException;
import com.intellisure.claimsservice.exception.ResourceNotFoundException;
import com.intellisure.claimsservice.repository.ClaimRepository;
import com.intellisure.claimsservice.security.SecurityActorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final CustomerPartyAdjusterClient customerPartyAdjusterClient;
    private final SecurityActorService securityActorService;

    public Flux<ClaimResponse> getClaims(UUID customerId) {
        Flux<Claim> claims = customerId == null ? claimRepository.findAll() : claimRepository.findByCustomerId(customerId);
        return claims.map(this::mapToResponse);
    }

    public Flux<ClaimResponse> getClaimsByStatus(String status) {
        return claimRepository.findByStatus(normalizeStatus(status)).map(this::mapToResponse);
    }

    public Flux<ClaimResponse> getClaimsForCaller(UUID requestedCustomerId, String status) {
        if (securityActorService == null) {
            if (status != null && !status.isBlank()) {
                return getClaimsByStatus(status);
            }
            return getClaims(requestedCustomerId);
        }
        return securityActorService.hasAnyRole("CLAIMS_ADJUSTER", "CLAIMS_MANAGER", "SYSTEM_ADMINISTRATOR", "ADMIN")
                .flatMapMany(isStaff -> {
                    if (Boolean.TRUE.equals(isStaff)) {
                        if (status != null && !status.isBlank()) {
                            return getClaimsByStatus(status);
                        }
                        return getClaims(requestedCustomerId);
                    }
                    return securityActorService.currentCustomerId()
                            .flatMapMany(myCustomerId -> {
                                if (status != null && !status.isBlank()) {
                                    return claimRepository.findByCustomerId(myCustomerId)
                                            .filter(c -> normalizeStatus(status).equals(c.getStatus()))
                                            .map(this::mapToResponse);
                                }
                                return getClaims(myCustomerId);
                            });
                });
    }

    public Mono<ClaimResponse> getClaim(UUID id) {
        return claimRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + id)))
                .flatMap(claim -> {
                    if (securityActorService != null) {
                        return securityActorService.assertClaimAccess(claim).thenReturn(claim);
                    }
                    return Mono.just(claim);
                })
                .map(this::mapToResponse);
    }

    public Mono<ClaimResponse> getClaimByNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimNumber)))
                .flatMap(claim -> {
                    if (securityActorService != null) {
                        return securityActorService.assertClaimAccess(claim).thenReturn(claim);
                    }
                    return Mono.just(claim);
                })
                .map(this::mapToResponse);
    }

    public Mono<ClaimResponse> updateStatus(UUID id, String status) {
        return claimRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + id)))
                .flatMap(claim -> {
                    String target = normalizeStatus(status);
                    validateStatusTransition(claim.getStatus(), target);
                    claim.setStatus(target);
                    claim.setUpdatedAt(LocalDateTime.now());
                    claim.setNew(false);
                    return claimRepository.save(claim);
                }).map(this::mapToResponse);
    }

    public void validateStatusTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            return;
        }
        if (currentStatus.equalsIgnoreCase(targetStatus)) {
            return;
        }
        if ("CLOSED".equalsIgnoreCase(currentStatus)) {
            throw new BusinessException("Cannot transition a CLOSED claim to " + targetStatus);
        }
        if ("DENIED".equalsIgnoreCase(currentStatus) && !"CLOSED".equalsIgnoreCase(targetStatus)) {
            throw new BusinessException("A DENIED claim can only transition to CLOSED");
        }
        if ("FNOL_RECEIVED".equalsIgnoreCase(currentStatus) &&
                ("SETTLED".equalsIgnoreCase(targetStatus) || "PAID".equalsIgnoreCase(targetStatus) || "CLOSED".equalsIgnoreCase(targetStatus))) {
            throw new BusinessException("Cannot transition directly from " + currentStatus + " to " + targetStatus + " without investigation and decision");
        }
    }

    public Mono<ClaimResponse> assignAdjuster(UUID claimId, UUID adjusterId) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> {
                    claim.setAssignedAdjusterId(adjusterId);
                    claim.setStatus(normalizeStatus("OPEN"));
                    claim.setUpdatedAt(LocalDateTime.now());
                    claim.setNew(false);
                    return claimRepository.save(claim);
                }).map(this::mapToResponse);
    }

    public Mono<ClaimResponse> resignAdjuster(UUID claimId) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> {
                    claim.setAssignedAdjusterId(null);
                    claim.setStatus(normalizeStatus("OPEN"));
                    claim.setUpdatedAt(LocalDateTime.now());
                    claim.setNew(false);
                    return claimRepository.save(claim);
                }).map(this::mapToResponse);
    }

    public Mono<ClaimResponse> createAssessment(UUID claimId,
                                               String causeOfLoss,
                                               String findings,
                                               boolean covered,
                                               BigDecimal totalLossAmount,
                                               BigDecimal coveredLossAmount,
                                               BigDecimal deductibleApplied,
                                               BigDecimal netLossAmount,
                                               UUID assessorId) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> {
                    claim.setIncidentType(causeOfLoss);
                    claim.setIncidentDescription(findings);
                    claim.setCoverageConfirmed(covered);
                    claim.setCoverageDecision(covered ? "APPROVED" : "DENIED");
                    claim.setStatus(normalizeStatus("COVERAGE_REVIEW"));
                    claim.setEstimatedCoveredLoss(coveredLossAmount == null ? BigDecimal.ZERO : coveredLossAmount);
                    claim.setUpdatedAt(LocalDateTime.now());
                    claim.setNew(false);
                    return claimRepository.save(claim);
                }).map(this::mapToResponse);
    }

    public Mono<ClaimResponse> createReserve(UUID claimId, BigDecimal reserve, String reserveReason, UUID updatedBy) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> {
                    claim.setEstimatedCoveredLoss(reserve == null ? BigDecimal.ZERO : reserve);
                    claim.setStatus(normalizeStatus("RESERVED"));
                    claim.setUpdatedAt(LocalDateTime.now());
                    claim.setNew(false);
                    return claimRepository.save(claim);
                }).map(this::mapToResponse);
    }

    public Mono<ClaimResponse> calculatePayout(UUID claimId, BigDecimal coveredLoss, BigDecimal deductible, BigDecimal policyLimit) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> {
                    BigDecimal effectiveLoss = coveredLoss == null ? BigDecimal.ZERO : coveredLoss;
                    BigDecimal effectiveDeductible = deductible == null ? BigDecimal.ZERO : deductible;
                    BigDecimal effectiveLimit = policyLimit == null ? BigDecimal.ZERO : policyLimit;

                    BigDecimal payable = effectiveLoss.subtract(effectiveDeductible);
                    if (payable.compareTo(BigDecimal.ZERO) < 0) {
                        payable = BigDecimal.ZERO;
                    }
                    if (effectiveLimit.compareTo(BigDecimal.ZERO) > 0 && payable.compareTo(effectiveLimit) > 0) {
                        payable = effectiveLimit;
                    }

                    claim.setPayoutAmount(payable);
                    claim.setStatus(normalizeStatus("RESERVED"));
                    claim.setUpdatedAt(LocalDateTime.now());
                    claim.setNew(false);
                    return claimRepository.save(claim);
                }).map(this::mapToResponse);
    }

    public Mono<ClaimResponse> approveSettlement(UUID claimId, UUID approvedBy, BigDecimal approvedAmount) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> {
                    claim.setPayoutAmount(approvedAmount == null ? claim.getPayoutAmount() : approvedAmount);
                    claim.setStatus(normalizeStatus("APPROVED"));
                    claim.setUpdatedAt(LocalDateTime.now());
                    claim.setNew(false);
                    return claimRepository.save(claim);
                }).map(this::mapToResponse);
    }

    public Mono<ClaimResponse> recordPayment(UUID claimId, String paymentReference) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> {
                    claim.setStatus(normalizeStatus("SETTLED"));
                    claim.setCoverageDecision("PAID");
                    claim.setUpdatedAt(LocalDateTime.now());
                    claim.setNew(false);
                    return claimRepository.save(claim);
                }).map(this::mapToResponse);
    }

    public Mono<ClaimResponse> recordClaimDecision(UUID claimId, String decision, String reason, UUID decidedBy) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> {
                    String normalizedDecision = decision.trim().toUpperCase();
                    if (!"APPROVED".equals(normalizedDecision) && !"DENIED".equals(normalizedDecision)) {
                        return Mono.error(new BusinessException("Claim decision must be APPROVED or DENIED"));
                    }
                    claim.setCoverageDecision(normalizedDecision);
                    claim.setCoverageDecisionReason(reason);
                    claim.setCoverageConfirmed("APPROVED".equals(normalizedDecision));
                    claim.setStatus(normalizeStatus(normalizedDecision));
                    claim.setUpdatedAt(LocalDateTime.now());
                    claim.setNew(false);
                    return claimRepository.save(claim);
                }).map(this::mapToResponse);
    }

    public Mono<ClaimResponse> closeClaim(UUID claimId, String closureReason) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> {
                    if (claim.getCoverageDecision() == null && !"DENIED".equalsIgnoreCase(claim.getStatus())) {
                        return Mono.error(new BusinessException("Cannot close claim without a coverage or claim decision"));
                    }
                    if ("APPROVED".equalsIgnoreCase(claim.getCoverageDecision())
                            && !"SETTLED".equalsIgnoreCase(claim.getStatus())
                            && !"PAID".equalsIgnoreCase(claim.getStatus())
                            && !"RECOVERY".equalsIgnoreCase(claim.getStatus())) {
                        return Mono.error(new BusinessException("Cannot close an approved claim until settlement, payment, or recovery is completed"));
                    }
                    claim.setStatus(normalizeStatus("CLOSED"));
                    claim.setClosureReason(closureReason == null ? "Closed" : closureReason);
                    claim.setUpdatedAt(LocalDateTime.now());
                    claim.setNew(false);
                    return claimRepository.save(claim);
                }).map(this::mapToResponse);
    }

    public Mono<ClaimResponse> fileClaim(FileClaimRequest request) {
        if (securityActorService != null) {
            return securityActorService.currentCustomerId()
                    .flatMap(customerId -> fileClaim(request, customerId));
        }
        return fileClaim(request, UUID.randomUUID());
    }

    public Mono<ClaimResponse> fileClaim(FileClaimRequest request, UUID customerId) {
        LocalDateTime now = LocalDateTime.now();

        Claim claim = Claim.builder()
                .claimId(UUID.randomUUID())
                .policyId(request.policyId())
                .customerId(customerId)
                .claimNumber("CLM-" + System.currentTimeMillis())
                .status(normalizeStatus("FNOL_RECEIVED"))
                .incidentDate(request.incidentDate())
                .reportedDate(LocalDate.now())
                .description(request.description())
                .estimatedLoss(request.estimatedLoss() == null ? BigDecimal.ZERO : request.estimatedLoss())
                .estimatedCoveredLoss(request.estimatedLoss() == null ? BigDecimal.ZERO : request.estimatedLoss())
                .coverageConfirmed(false)
                .createdAt(now)
                .updatedAt(now)
                .isNew(true)
                .build();

        return claimRepository.save(claim)
                .flatMap(saved -> autoAssignAdjuster(saved)
                        .flatMap(adjusterId -> {
                            saved.setAssignedAdjusterId(adjusterId);
                            saved.setStatus(normalizeStatus("OPEN"));
                            saved.setUpdatedAt(LocalDateTime.now());
                            saved.setNew(false);
                            return claimRepository.save(saved);
                        })
                        .switchIfEmpty(Mono.just(saved))
                )
                .map(this::mapToResponse);
    }

    private Mono<UUID> autoAssignAdjuster(Claim claim) {
        if (customerPartyAdjusterClient == null) {
            return Mono.empty();
        }

        return customerPartyAdjusterClient.findAvailableAdjusters()
                .onErrorResume(err -> {
                    log.warn("Adjuster lookup failed during auto-assignment: {}", err.getMessage());
                    return Flux.empty();
                })
                .collectList()
                .flatMap(adjusters -> {
                    if (adjusters == null || adjusters.isEmpty()) {
                        return Mono.empty();
                    }

                    return claimRepository.findAll()
                            .collectList()
                            .map(existingClaims -> {
                                Map<UUID, Integer> activeClaimCounts = new HashMap<>();
                                for (Claim existingClaim : existingClaims) {
                                    UUID assignedAdjusterId = existingClaim.getAssignedAdjusterId();
                                    if (assignedAdjusterId == null || !adjusters.contains(assignedAdjusterId) || !isActiveClaim(existingClaim.getStatus())) {
                                        continue;
                                    }
                                    activeClaimCounts.merge(assignedAdjusterId, 1, Integer::sum);
                                }

                                return adjusters.stream()
                                        .sorted((left, right) -> {
                                            int leftLoad = activeClaimCounts.getOrDefault(left, 0);
                                            int rightLoad = activeClaimCounts.getOrDefault(right, 0);
                                            if (leftLoad != rightLoad) {
                                                return Integer.compare(leftLoad, rightLoad);
                                            }
                                            return left.toString().compareTo(right.toString());
                                        })
                                        .findFirst()
                                        .orElse(null);
                            })
                            .flatMap(selected -> selected == null ? Mono.empty() : Mono.just(selected));
                });
    }

    private boolean isActiveClaim(String status) {
        if (status == null) {
            return false;
        }
        String normalized = normalizeStatus(status);
        return !List.of("CLOSED", "SETTLED", "PAID").contains(normalized);
    }

    private ClaimResponse mapToResponse(Claim claim) {
        return new ClaimResponse(
                claim.getClaimId(),
                claim.getPolicyId(),
                claim.getCustomerId(),
                claim.getClaimNumber(),
                claim.getStatus(),
                claim.getIncidentDate(),
                claim.getReportedDate(),
                claim.getDescription(),
                claim.getEstimatedLoss(),
                claim.getPayoutAmount(),
                claim.getPayoutAmount(),
                claim.getCreatedAt(),
                claim.getUpdatedAt(),
                claim.getIncidentType(),
                claim.getIncidentLocation(),
                claim.getCoverageDecision(),
                claim.getAssignedAdjusterId(),
                claim.getCoverageConfirmed(),
                claim.getClosureReason()
        );
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "OPEN";
        }
        try {
            return rawStatus.trim().toUpperCase(Locale.ROOT);
        } catch (IllegalArgumentException ex) {
            return rawStatus.trim();
        }
    }
}
