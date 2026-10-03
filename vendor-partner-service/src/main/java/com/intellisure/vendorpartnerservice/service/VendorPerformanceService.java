package com.intellisure.vendorpartnerservice.service;

import com.intellisure.vendorpartnerservice.dto.RecordVendorPerformanceRequest;
import com.intellisure.vendorpartnerservice.dto.VendorPerformanceResponse;
import com.intellisure.vendorpartnerservice.entity.AssignmentStatus;
import com.intellisure.vendorpartnerservice.entity.VendorAssignment;
import com.intellisure.vendorpartnerservice.entity.VendorPerformance;
import com.intellisure.vendorpartnerservice.repository.VendorAssignmentRepository;
import com.intellisure.vendorpartnerservice.repository.VendorPerformanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendorPerformanceService {

    private final VendorPerformanceRepository performanceRepository;
    private final VendorAssignmentRepository assignmentRepository;

    public Mono<VendorPerformanceResponse> recordPerformance(RecordVendorPerformanceRequest request) {
        return assignmentRepository.findById(request.assignmentId())
                .flatMap(assignment -> {
                    if (assignment.getStatus() != AssignmentStatus.COMPLETED) {
                        return Mono.error(new IllegalStateException("Can only record performance for completed assignments"));
                    }
                    
                    BigDecimal overallScore = calculateOverallScore(
                            request.qualityScore(),
                            request.timelinessScore(),
                            request.communicationScore(),
                            request.outcomeScore()
                    );
                    
                    VendorPerformance performance = VendorPerformance.builder()
                            .performanceId(UUID.randomUUID())
                            .vendorId(assignment.getVendorId())
                            .assignmentId(request.assignmentId())
                            .qualityScore(request.qualityScore())
                            .timelinessScore(request.timelinessScore())
                            .communicationScore(request.communicationScore())
                            .outcomeScore(request.outcomeScore())
                            .overallScore(overallScore)
                            .note(request.note())
                            .recordedAt(LocalDateTime.now())
                            .isNew(true)
                            .build();
                    
                    return performanceRepository.save(performance)
                            .map(this::mapToResponse);
                });
    }

    public Flux<VendorPerformanceResponse> getVendorPerformance(UUID vendorId) {
        return performanceRepository.findByVendorId(vendorId)
                .map(this::mapToResponse);
    }

    public Flux<VendorPerformanceResponse> getVendorPerformance(UUID vendorId, LocalDateTime start, LocalDateTime end) {
        return performanceRepository.findByVendorIdAndRecordedAtBetween(vendorId, start, end)
                .map(this::mapToResponse);
    }

    private BigDecimal calculateOverallScore(BigDecimal quality, BigDecimal timeliness, BigDecimal communication, BigDecimal outcome) {
        return quality.add(timeliness)
                .add(communication)
                .add(outcome)
                .divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
    }

    private VendorPerformanceResponse mapToResponse(VendorPerformance entity) {
        return new VendorPerformanceResponse(
                entity.getVendorId(),
                entity.getAssignmentId(),
                entity.getQualityScore(),
                entity.getTimelinessScore(),
                entity.getCommunicationScore(),
                entity.getOutcomeScore(),
                entity.getOverallScore(),
                entity.getNote(),
                entity.getRecordedAt()
        );
    }
}