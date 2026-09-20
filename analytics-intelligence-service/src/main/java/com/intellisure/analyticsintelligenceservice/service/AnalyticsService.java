package com.intellisure.analyticsintelligenceservice.service;

import com.intellisure.analyticsintelligenceservice.dto.GenerateRiskScoreRequest;
import com.intellisure.analyticsintelligenceservice.dto.RiskScoreResponse;
import com.intellisure.analyticsintelligenceservice.entity.RiskScoreSnapshot;
import com.intellisure.analyticsintelligenceservice.repository.RiskScoreSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final RiskScoreSnapshotRepository riskScoreSnapshotRepository;

    public Mono<RiskScoreResponse> generateRiskScore(GenerateRiskScoreRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        RiskScoreSnapshot snapshot = RiskScoreSnapshot.builder()
                .snapshotId(UUID.randomUUID())
                .customerId(request.customerId())
                .riskScore(new BigDecimal("75.5")) // Mocked AI generation
                .riskBand("MEDIUM")
                .keyFactors("Recent claims history, Location risk")
                .modelVersion("v1.2.0")
                .generatedAt(now)
                .isNew(true)
                .build();

        return riskScoreSnapshotRepository.save(snapshot)
                .map(this::mapToResponse);
    }

    private RiskScoreResponse mapToResponse(RiskScoreSnapshot snapshot) {
        return new RiskScoreResponse(
                snapshot.getSnapshotId(),
                snapshot.getCustomerId(),
                snapshot.getRiskScore(),
                snapshot.getRiskBand(),
                snapshot.getKeyFactors(),
                snapshot.getModelVersion(),
                snapshot.getGeneratedAt()
        );
    }
}
