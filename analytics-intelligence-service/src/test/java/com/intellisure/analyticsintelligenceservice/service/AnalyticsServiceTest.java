package com.intellisure.analyticsintelligenceservice.service;

import com.intellisure.analyticsintelligenceservice.dto.GenerateRiskScoreRequest;
import com.intellisure.analyticsintelligenceservice.entity.RiskScoreSnapshot;
import com.intellisure.analyticsintelligenceservice.repository.RiskScoreSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    @Mock RiskScoreSnapshotRepository repository;
    @InjectMocks AnalyticsService service;

    @Test
    void generatesAndMapsMediumRiskSnapshot() {
        UUID customer = UUID.randomUUID();
        RiskScoreSnapshot saved = RiskScoreSnapshot.builder().snapshotId(UUID.randomUUID())
                .customerId(customer).riskScore(new java.math.BigDecimal("75.5"))
                .riskBand("MEDIUM").keyFactors("claims").modelVersion("v1.2.0").build();
        when(repository.save(any(RiskScoreSnapshot.class))).thenReturn(Mono.just(saved));
        StepVerifier.create(service.generateRiskScore(new GenerateRiskScoreRequest(customer)))
                .assertNext(r -> { assertEquals(customer, r.customerId()); assertEquals("MEDIUM", r.riskBand()); })
                .verifyComplete();
        verify(repository).save(any(RiskScoreSnapshot.class));
    }

    @Test
    void propagatesPersistenceFailure() {
        when(repository.save(any())).thenReturn(Mono.error(new IllegalStateException("database")));
        StepVerifier.create(service.generateRiskScore(new GenerateRiskScoreRequest(UUID.randomUUID())))
                .expectErrorMessage("database").verify();
    }
}
