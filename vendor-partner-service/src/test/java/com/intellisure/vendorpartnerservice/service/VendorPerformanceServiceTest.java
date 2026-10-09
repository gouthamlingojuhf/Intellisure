package com.intellisure.vendorpartnerservice.service;

import com.intellisure.vendorpartnerservice.dto.RecordVendorPerformanceRequest;
import com.intellisure.vendorpartnerservice.entity.AssignmentStatus;
import com.intellisure.vendorpartnerservice.entity.VendorAssignment;
import com.intellisure.vendorpartnerservice.entity.VendorPerformance;
import com.intellisure.vendorpartnerservice.repository.VendorAssignmentRepository;
import com.intellisure.vendorpartnerservice.repository.VendorPerformanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorPerformanceServiceTest {
    @Mock VendorPerformanceRepository performanceRepository;
    @Mock VendorAssignmentRepository assignmentRepository;
    @InjectMocks VendorPerformanceService service;

    @Test
    void recordsPerformanceOnlyForCompletedAssignmentAndCalculatesAverage() {
        UUID assignmentId = UUID.randomUUID(); UUID vendorId = UUID.randomUUID();
        when(assignmentRepository.findById(assignmentId)).thenReturn(Mono.just(VendorAssignment.builder()
                .assignmentId(assignmentId).vendorId(vendorId).status(AssignmentStatus.COMPLETED).build()));
        when(performanceRepository.save(any(VendorPerformance.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        RecordVendorPerformanceRequest request = new RecordVendorPerformanceRequest(assignmentId,
                BigDecimal.valueOf(90), BigDecimal.valueOf(80), BigDecimal.valueOf(70), BigDecimal.valueOf(60), "Good");
        StepVerifier.create(service.recordPerformance(request))
                .assertNext(response -> assertEquals(BigDecimal.valueOf(75.00).setScale(2), response.overallScore())).verifyComplete();
    }

    @Test
    void rejectsIncompleteAssignmentAndSupportsBothPerformanceQueries() {
        UUID id = UUID.randomUUID();
        when(assignmentRepository.findById(id)).thenReturn(Mono.just(VendorAssignment.builder().assignmentId(id).status(AssignmentStatus.IN_PROGRESS).build()));
        StepVerifier.create(service.recordPerformance(new RecordVendorPerformanceRequest(id, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, null)))
                .expectError(IllegalStateException.class).verify();
        VendorPerformance performance = VendorPerformance.builder().vendorId(UUID.randomUUID()).assignmentId(id)
                .qualityScore(BigDecimal.ONE).timelinessScore(BigDecimal.ONE).communicationScore(BigDecimal.ONE).outcomeScore(BigDecimal.ONE)
                .overallScore(BigDecimal.ONE).recordedAt(LocalDateTime.now()).build();
        when(performanceRepository.findByVendorId(performance.getVendorId())).thenReturn(Flux.just(performance));
        when(performanceRepository.findByVendorIdAndRecordedAtBetween(any(), any(), any())).thenReturn(Flux.just(performance));
        StepVerifier.create(service.getVendorPerformance(performance.getVendorId())).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getVendorPerformance(performance.getVendorId(), LocalDateTime.now().minusDays(1), LocalDateTime.now())).expectNextCount(1).verifyComplete();
    }
}
