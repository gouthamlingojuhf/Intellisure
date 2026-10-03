package com.intellisure.quotepolicyservice.mapper;

import com.intellisure.quotepolicyservice.dto.response.PolicyCoverageResponse;
import com.intellisure.quotepolicyservice.dto.response.PolicyResponse;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.PolicyCoverage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PolicyMapper {

    public PolicyCoverageResponse toCoverageResponse(
            PolicyCoverage coverage
    ) {
        return new PolicyCoverageResponse(
                coverage.getPolicyCoverageId(),
                coverage.getCoverageCode(),
                coverage.getCoverageName(),
                coverage.getLimitAmount(),
                coverage.getDeductibleAmount(),
                coverage.getCoveragePremium(),
                coverage.getConditions(),
                coverage.getExclusions(),
                coverage.getWaitingPeriodDays(),
                coverage.getEffectiveFrom(),
                coverage.getEffectiveTo(),
                coverage.getCreatedAt()
        );
    }

    public PolicyResponse toResponse(
            Policy policy,
            List<PolicyCoverage> coverages
    ) {
        List<PolicyCoverageResponse> coverageResponses =
                coverages.stream()
                        .map(this::toCoverageResponse)
                        .toList();

        return new PolicyResponse(
                policy.getPolicyId(),
                policy.getPolicyNumber(),
                policy.getQuoteId(),
                policy.getCustomerId(),
                policy.getProductCode(),
                policy.getStatus(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getTotalPremium(),
                policy.getIssuedByUserId(),
                policy.getBoundAt(),
                policy.getIssuedAt(),
                policy.getExpiredAt(),
                policy.getCreatedAt(),
                policy.getUpdatedAt(),
                coverageResponses
        );
    }
}