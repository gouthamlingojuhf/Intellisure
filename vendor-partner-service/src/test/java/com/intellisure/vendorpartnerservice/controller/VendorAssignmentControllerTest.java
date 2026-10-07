package com.intellisure.vendorpartnerservice.controller;

import com.intellisure.vendorpartnerservice.dto.AcceptAssignmentRequest;
import com.intellisure.vendorpartnerservice.dto.CreateVendorAssignmentRequest;
import com.intellisure.vendorpartnerservice.dto.DeclineAssignmentRequest;
import com.intellisure.vendorpartnerservice.dto.UpdateAssignmentStatusRequest;
import com.intellisure.vendorpartnerservice.dto.VendorAssignmentResponse;
import com.intellisure.vendorpartnerservice.service.VendorAssignmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendorAssignmentControllerTest")
class VendorAssignmentControllerTest {

    @Mock
    private VendorAssignmentService assignmentService;

    @InjectMocks
    private VendorAssignmentController controller;

    @Test
    @DisplayName("POST /api/vendor-assignments delegates assignment creation to service")
    void createAssignmentDelegatesToService() {
        UUID vendorId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        CreateVendorAssignmentRequest request = new CreateVendorAssignmentRequest(
                vendorId, "RESTORATION", UUID.randomUUID(), UUID.randomUUID(), "NETWORK_VENDOR",
                "Restoration task", LocalDate.now().plusWeeks(2), "HIGH"
        );

        VendorAssignmentResponse response = new VendorAssignmentResponse(
                assignmentId, vendorId, "RESTORATION", request.claimId(), request.recoveryCaseId(),
                "DISPATCHED", "Restoration task", request.dueDate(), "HIGH", null, null,
                null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(assignmentService.createAssignment(eq(request))).thenReturn(Mono.just(response));

        StepVerifier.create(controller.createAssignment(request))
                .assertNext(res -> {
                    assertEquals(assignmentId, res.assignmentId());
                    assertEquals("DISPATCHED", res.status());
                    assertEquals("RESTORATION", res.assignmentType());
                })
                .verifyComplete();

        verify(assignmentService).createAssignment(eq(request));
    }

    @Test
    @DisplayName("POST /api/vendor-assignments/{id}/accept delegates acceptance to service")
    void acceptAssignmentDelegatesToService() {
        UUID assignmentId = UUID.randomUUID();
        AcceptAssignmentRequest request = new AcceptAssignmentRequest("Accepted", LocalDate.now());

        VendorAssignmentResponse response = new VendorAssignmentResponse(
                assignmentId, UUID.randomUUID(), "RESTORATION", UUID.randomUUID(), UUID.randomUUID(),
                "ACCEPTED", "Restoration task", LocalDate.now().plusWeeks(2), "HIGH",
                LocalDateTime.now(), null, null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(assignmentService.acceptAssignment(eq(assignmentId), eq(request))).thenReturn(Mono.just(response));

        StepVerifier.create(controller.acceptAssignment(assignmentId, request))
                .assertNext(res -> assertEquals("ACCEPTED", res.status()))
                .verifyComplete();

        verify(assignmentService).acceptAssignment(eq(assignmentId), eq(request));
    }

    @Test
    @DisplayName("POST /api/vendor-assignments/{id}/decline delegates decline to service")
    void declineAssignmentDelegatesToService() {
        UUID assignmentId = UUID.randomUUID();
        DeclineAssignmentRequest request = new DeclineAssignmentRequest("Unavailable");

        VendorAssignmentResponse response = new VendorAssignmentResponse(
                assignmentId, UUID.randomUUID(), "RESTORATION", UUID.randomUUID(), UUID.randomUUID(),
                "DECLINED", "Restoration task", LocalDate.now().plusWeeks(2), "HIGH",
                null, null, null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(assignmentService.declineAssignment(eq(assignmentId), eq(request))).thenReturn(Mono.just(response));

        StepVerifier.create(controller.declineAssignment(assignmentId, request))
                .assertNext(res -> assertEquals("DECLINED", res.status()))
                .verifyComplete();

        verify(assignmentService).declineAssignment(eq(assignmentId), eq(request));
    }

    @Test
    @DisplayName("PATCH /api/vendor-assignments/{id}/status delegates status update to service")
    void updateAssignmentStatusDelegatesToService() {
        UUID assignmentId = UUID.randomUUID();
        UpdateAssignmentStatusRequest request = new UpdateAssignmentStatusRequest(
                "IN_PROGRESS", "Working on site", LocalDate.now(), null
        );

        VendorAssignmentResponse response = new VendorAssignmentResponse(
                assignmentId, UUID.randomUUID(), "RESTORATION", UUID.randomUUID(), UUID.randomUUID(),
                "IN_PROGRESS", "Restoration task", LocalDate.now().plusWeeks(2), "HIGH",
                LocalDateTime.now(), null, null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(assignmentService.updateAssignmentStatus(eq(assignmentId), eq(request))).thenReturn(Mono.just(response));

        StepVerifier.create(controller.updateAssignmentStatus(assignmentId, request))
                .assertNext(res -> assertEquals("IN_PROGRESS", res.status()))
                .verifyComplete();

        verify(assignmentService).updateAssignmentStatus(eq(assignmentId), eq(request));
    }
}
