package com.intellisure.vendorpartnerservice.controller;

import com.intellisure.vendorpartnerservice.dto.*;
import com.intellisure.vendorpartnerservice.entity.*;
import com.intellisure.vendorpartnerservice.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllerCoverageTest {
    @Mock VendorAssignmentService assignmentService; @Mock VendorService vendorService; @Mock VendorOnboardingService onboardingService; @Mock VendorPerformanceService performanceService;
    @InjectMocks VendorAssignmentController assignmentController; @InjectMocks VendorController vendorController; @InjectMocks VendorOnboardingController onboardingController; @InjectMocks VendorPerformanceController performanceController;

    @Test
    void assignmentAndVendorControllersDelegate() {
        UUID id = UUID.randomUUID();
        CreateVendorAssignmentRequest create = new CreateVendorAssignmentRequest(id, "RESTORATION", id, id, "NETWORK_VENDOR", "Task", LocalDate.now(), "HIGH");
        when(assignmentService.createAssignment(create)).thenReturn(Mono.empty()); when(assignmentService.getAssignment(id)).thenReturn(Mono.empty());
        when(assignmentService.getAssignments(any())).thenReturn(Flux.empty()); when(assignmentService.acceptAssignment(eq(id), any())).thenReturn(Mono.empty());
        when(assignmentService.declineAssignment(eq(id), any())).thenReturn(Mono.empty()); when(assignmentService.updateAssignmentStatus(eq(id), any())).thenReturn(Mono.empty());
        assignmentController.createAssignment(create); assignmentController.getAssignment(id); assignmentController.getAssignments(id, id, id, "RESTORATION", "DISPATCHED", LocalDate.now(), LocalDate.now(), 0, 20);
        assignmentController.acceptAssignment(id, new AcceptAssignmentRequest("yes", LocalDate.now())); assignmentController.declineAssignment(id, new DeclineAssignmentRequest("no"));
        assignmentController.updateAssignmentStatus(id, new UpdateAssignmentStatusRequest("IN_PROGRESS", "start", null, null));

        when(vendorService.getVendor(id)).thenReturn(Mono.empty()); when(vendorService.searchVendors(any())).thenReturn(Mono.empty()); when(vendorService.recommendVendors(any())).thenReturn(Mono.empty());
        when(vendorService.updateVendor(eq(id), any())).thenReturn(Mono.empty()); when(vendorService.updateVendorStatus(eq(id), any())).thenReturn(Mono.empty());
        vendorController.getVendor(id); vendorController.searchVendors("A", "B", BigDecimal.ONE, "C", "D", 0, 20); vendorController.getRecommendations(null, null, null, null, null, 0, 20);
        vendorController.updateVendor(id, new UpdateVendorRequest("Display", List.of(), List.of(), List.of(), "Name", "Phone", "Email"));
        vendorController.updateVendorStatus(id, new UpdateVendorStatusRequest("ACTIVE", "ready"));
    }

    @Test
    void onboardingAndPerformanceControllersDelegateEveryEndpoint() {
        UUID id = UUID.randomUUID();
        com.intellisure.vendorpartnerservice.dto.VendorOnboardingRequest onboarding = new com.intellisure.vendorpartnerservice.dto.VendorOnboardingRequest("Legal", "Display", "TOWING", List.of("TOWING"), List.of("CAP"), List.of("AREA"), "Name", "Phone", "Email", List.of());
        when(onboardingService.submitOnboardingRequest(onboarding)).thenReturn(Mono.empty()); when(onboardingService.getOnboardingRequests()).thenReturn(Flux.empty()); when(onboardingService.verifyVendor(eq(id), any())).thenReturn(Mono.empty());
        onboardingController.submitOnboardingRequest(onboarding); onboardingController.getOnboardingRequests(); onboardingController.verifyVendor(id, new VerifyVendorRequest("APPROVE", null, List.of()));
        RecordVendorPerformanceRequest performance = new RecordVendorPerformanceRequest(id, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, "note");
        when(performanceService.recordPerformance(performance)).thenReturn(Mono.empty()); when(performanceService.getVendorPerformance(id)).thenReturn(Flux.empty()); when(performanceService.getVendorPerformance(eq(id), any(), any())).thenReturn(Flux.empty());
        performanceController.recordPerformance(id, performance); performanceController.getVendorPerformance(id, null, null); performanceController.getVendorPerformance(id, LocalDateTime.now().minusDays(1), null); performanceController.getVendorPerformance(id, LocalDateTime.now().minusDays(1), LocalDateTime.now());
    }
}
