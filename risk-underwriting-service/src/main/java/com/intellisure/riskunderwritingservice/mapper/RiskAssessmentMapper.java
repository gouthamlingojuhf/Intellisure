package com.intellisure.riskunderwritingservice.mapper;

import com.intellisure.riskunderwritingservice.dto.response.RiskAssessmentResponse;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import org.springframework.stereotype.Component;

@Component
public class RiskAssessmentMapper {

    public RiskAssessmentResponse toResponse(
            RiskAssessment assessment
    ) {
        return new RiskAssessmentResponse(
                assessment.getAssessmentId(),
                assessment.getAssessmentNumber(),
                assessment.getQuoteId(),
                assessment.getPolicyId(),
                assessment.getCustomerId(),
                assessment.getAssessmentType(),
                assessment.getStatus(),
                assessment.getAssessmentDate(),
                assessment.getLocation(),
                assessment.getBusinessOperations(),
                assessment.getAnnualRevenue(),
                assessment.getAnnualPayroll(),
                assessment.getEmployeeCount(),
                assessment.getAssetValue(),
                assessment.getPriorClaimCount(),
                assessment.getPriorLossAmount(),
                assessment.getRiskScore(),
                assessment.getRiskBand(),
                assessment.getSummary(),
                assessment.getCreatedBy(),
                assessment.getAssignedUnderwriterId(),
                assessment.getAssignedRiskEngineerId(),
                assessment.getSubmittedAt(),
                assessment.getCompletedAt(),
                assessment.getCreatedAt(),
                assessment.getUpdatedAt()
        );
    }
}