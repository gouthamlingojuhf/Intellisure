package com.intellisure.recoveryservice.controller;

import com.intellisure.recoveryservice.dto.RecordRecoveryProgressRequest;
import com.intellisure.recoveryservice.dto.RecoveryCaseResponse;
import com.intellisure.recoveryservice.dto.SelectRecoveryPathRequest;
import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import com.intellisure.recoveryservice.entity.RecoveryPath;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
import com.intellisure.recoveryservice.service.RecoveryCaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecoveryCaseControllerTest")
class RecoveryCaseControllerTest {

    @Mock
    private RecoveryCaseService recoveryCaseService;

    @InjectMocks
    private RecoveryCaseController controller;

    @Test
    @DisplayName("POST /path delegates path selection to service")
    void selectRecoveryPathDelegatesToService() {
        UUID caseId = UUID.randomUUID();
        SelectRecoveryPathRequest request = new SelectRecoveryPathRequest(
                RecoveryPath.NETWORK_VENDOR, UUID.randomUUID(), "Restoration", LocalDate.now().plusDays(7), "Notes");

        RecoveryCaseResponse response = new RecoveryCaseResponse(
                caseId, UUID.randomUUID(), UUID.randomUUID(), RecoverySeverity.HIGH,
                RecoveryCaseStatus.PLANNING, RecoveryPath.NETWORK_VENDOR,
                "Restoration", "Notes", LocalDate.now().plusDays(7), null,
                BigDecimal.ZERO, UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now());

        when(recoveryCaseService.selectRecoveryPath(eq(caseId), eq(request))).thenReturn(Mono.just(response));

        StepVerifier.create(controller.selectRecoveryPath(caseId, request))
                .assertNext(res -> {
                    assertEquals(RecoveryPath.NETWORK_VENDOR, res.recoveryPath());
                    assertEquals(RecoveryCaseStatus.PLANNING, res.status());
                })
                .verifyComplete();

        verify(recoveryCaseService).selectRecoveryPath(eq(caseId), eq(request));
    }

    @Test
    @DisplayName("PUT /path delegates to service")
    void updateRecoveryPathDelegatesToService() {
        UUID caseId = UUID.randomUUID();
        SelectRecoveryPathRequest request = new SelectRecoveryPathRequest(
                RecoveryPath.CUSTOMER_VENDOR, null, null, null, "Customer vendor notes");

        RecoveryCaseResponse response = new RecoveryCaseResponse(
                caseId, UUID.randomUUID(), UUID.randomUUID(), RecoverySeverity.MEDIUM,
                RecoveryCaseStatus.PLANNING, RecoveryPath.CUSTOMER_VENDOR,
                "Objective", "Customer vendor notes", null, null,
                BigDecimal.ZERO, null, LocalDateTime.now(), LocalDateTime.now());

        when(recoveryCaseService.selectRecoveryPath(eq(caseId), eq(request))).thenReturn(Mono.just(response));

        StepVerifier.create(controller.updateRecoveryPath(caseId, request))
                .assertNext(res -> {
                    assertEquals(RecoveryPath.CUSTOMER_VENDOR, res.recoveryPath());
                    assertEquals("Customer vendor notes", res.recoveryNotes());
                })
                .verifyComplete();

        verify(recoveryCaseService).selectRecoveryPath(eq(caseId), eq(request));
    }

    @Test
    @DisplayName("POST /select-path alias delegates to service")
    void selectPathAliasDelegatesToService() {
        UUID caseId = UUID.randomUUID();
        SelectRecoveryPathRequest request = new SelectRecoveryPathRequest(
                RecoveryPath.CUSTOMER_MANAGED, null, null, null, "Self managed");

        RecoveryCaseResponse response = new RecoveryCaseResponse(
                caseId, UUID.randomUUID(), UUID.randomUUID(), RecoverySeverity.LOW,
                RecoveryCaseStatus.PLANNING, RecoveryPath.CUSTOMER_MANAGED,
                "Objective", "Self managed", null, null,
                BigDecimal.ZERO, null, LocalDateTime.now(), LocalDateTime.now());

        when(recoveryCaseService.selectRecoveryPath(eq(caseId), eq(request))).thenReturn(Mono.just(response));

        StepVerifier.create(controller.selectRecoveryPathAlias(caseId, request))
                .assertNext(res -> {
                    assertEquals(RecoveryPath.CUSTOMER_MANAGED, res.recoveryPath());
                })
                .verifyComplete();

        verify(recoveryCaseService).selectRecoveryPath(eq(caseId), eq(request));
    }

    @Test
    @DisplayName("POST /progress delegates progress recording to service")
    void recordProgressDelegatesToService() {
        UUID caseId = UUID.randomUUID();
        RecordRecoveryProgressRequest request = new RecordRecoveryProgressRequest(
                BigDecimal.valueOf(75), "75 percent restored");

        RecoveryCaseResponse response = new RecoveryCaseResponse(
                caseId, UUID.randomUUID(), UUID.randomUUID(), RecoverySeverity.HIGH,
                RecoveryCaseStatus.BUSINESS_PARTIALLY_RESTORED, RecoveryPath.CUSTOMER_MANAGED,
                "Objective", "75 percent restored", null, null,
                BigDecimal.valueOf(75), null, LocalDateTime.now(), LocalDateTime.now());

        when(recoveryCaseService.recordProgress(eq(caseId), eq(request))).thenReturn(Mono.just(response));

        StepVerifier.create(controller.recordProgress(caseId, request))
                .assertNext(res -> {
                    assertEquals(BigDecimal.valueOf(75), res.currentRestorePercent());
                    assertEquals(RecoveryCaseStatus.BUSINESS_PARTIALLY_RESTORED, res.status());
                })
                .verifyComplete();

        verify(recoveryCaseService).recordProgress(eq(caseId), eq(request));
    }
}
