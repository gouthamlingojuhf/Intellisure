package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.CreateRiskAssessmentRequest;
import com.intellisure.riskunderwritingservice.dto.RiskAssessmentResponse;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import com.intellisure.riskunderwritingservice.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RiskAssessmentService {

    private final RiskAssessmentRepository riskAssessmentRepository;

    public Mono<RiskAssessmentResponse> createAssessment(CreateRiskAssessmentRequest request, UUID createdBy) {
        LocalDateTime now = LocalDateTime.now();
        RiskAssessment assessment = RiskAssessment.builder()
                .assessmentId(UUID.randomUUID())
                .quoteId(request.quoteId())
                .policyId(request.policyId())
                .customerId(request.customerId())
                .assessmentType(request.assessmentType())
                .status("DRAFT")
                .assessmentDate(LocalDate.now())
                .location(request.location())
                .businessOperations(request.businessOperations())
                .riskScore(request.riskScore())
                .summary(request.summary())
                .createdBy(createdBy)
                .createdAt(now)
                .updatedAt(now)
                .isNew(true)
                .build();

        return riskAssessmentRepository.save(assessment)
                .map(this::mapToResponse);
    }

    private RiskAssessmentResponse mapToResponse(RiskAssessment assessment) {
        return new RiskAssessmentResponse(
                assessment.getAssessmentId(),
                assessment.getQuoteId(),
                assessment.getPolicyId(),
                assessment.getCustomerId(),
                assessment.getAssessmentType(),
                assessment.getStatus(),
                assessment.getAssessmentDate(),
                assessment.getLocation(),
                assessment.getBusinessOperations(),
                assessment.getRiskScore(),
                assessment.getSummary(),
                assessment.getCreatedAt(),
                assessment.getUpdatedAt()
        );
    }
}
