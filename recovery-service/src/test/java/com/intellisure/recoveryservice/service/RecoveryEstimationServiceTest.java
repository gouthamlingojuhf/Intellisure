package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.dto.RecoveryEstimationRequest;
import com.intellisure.recoveryservice.dto.RecoveryEstimationResponse;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecoveryEstimationServiceTest {
    private final RecoveryEstimationService service = new RecoveryEstimationService();

    @Test
    void calculatesCappedSubrogationProvidedSalvageAndCatastrophicReinsurance() throws Exception {
        UUID claim = UUID.randomUUID();
        RecoveryEstimationResponse response = service.estimateRecoveryAsync(new RecoveryEstimationRequest(
                claim, UUID.randomUUID(), BigDecimal.valueOf(300000), 0.8, RecoverySeverity.HIGH,
                BigDecimal.valueOf(100000), BigDecimal.valueOf(2500))).get();
        assertEquals(claim, response.claimId());
        assertEquals(BigDecimal.valueOf(100000), response.subrogationEstimate());
        assertEquals(BigDecimal.valueOf(2500), response.salvageEstimate());
        assertEquals(BigDecimal.valueOf(45000).setScale(2), response.reinsuranceEstimate());
        assertTrue(response.estimationNotes().contains("catastrophic"));
        assertTrue(response.estimationNotes().contains("$100000"));
    }

    @Test
    void estimatesSalvageForEverySeverityAndHandlesNonCatastrophicNoLimit() throws Exception {
        BigDecimal claim = BigDecimal.valueOf(1000);
        for (RecoverySeverity severity : RecoverySeverity.values()) {
            RecoveryEstimationResponse response = service.estimateRecoveryAsync(new RecoveryEstimationRequest(
                    UUID.randomUUID(), UUID.randomUUID(), claim, 0.25, severity, null, null)).get();
            assertEquals(BigDecimal.ZERO, response.reinsuranceEstimate());
            assertTrue(response.salvageEstimate().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(response.estimationNotes().contains("N/A"));
        }
    }
}
