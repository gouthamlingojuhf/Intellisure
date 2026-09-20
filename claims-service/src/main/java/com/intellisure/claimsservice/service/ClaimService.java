package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.dto.ClaimResponse;
import com.intellisure.claimsservice.dto.FileClaimRequest;
import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;

    public Flux<ClaimResponse> getClaims(UUID customerId) {
        Flux<Claim> claims = customerId == null ? claimRepository.findAll() : claimRepository.findByCustomerId(customerId);
        return claims.map(this::mapToResponse);
    }

    public Mono<ClaimResponse> getClaim(UUID id) {
        return claimRepository.findById(id).map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + id)));
    }

    public Mono<ClaimResponse> updateStatus(UUID id, String status) {
        return claimRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + id)))
                .flatMap(claim -> {
                    claim.setStatus(status);
                    claim.setUpdatedAt(LocalDateTime.now());
                    claim.setNew(false);
                    return claimRepository.save(claim);
                }).map(this::mapToResponse);
    }

    public Mono<ClaimResponse> fileClaim(FileClaimRequest request, UUID customerId) {
        LocalDateTime now = LocalDateTime.now();
        
        Claim claim = Claim.builder()
                .claimId(UUID.randomUUID())
                .policyId(request.policyId())
                .customerId(customerId)
                .claimNumber("CLM-" + System.currentTimeMillis())
                .status("FILED")
                .incidentDate(request.incidentDate())
                .reportedDate(LocalDate.now())
                .description(request.description())
                .estimatedLoss(request.estimatedLoss())
                .createdAt(now)
                .updatedAt(now)
                .isNew(true)
                .build();

        return claimRepository.save(claim)
                .map(this::mapToResponse);
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
                claim.getCreatedAt(),
                claim.getUpdatedAt()
        );
    }
}
