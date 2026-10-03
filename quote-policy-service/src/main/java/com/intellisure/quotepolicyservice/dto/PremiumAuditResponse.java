package com.intellisure.quotepolicyservice.dto;

import com.intellisure.quotepolicyservice.enums.AuditType;
import com.intellisure.quotepolicyservice.enums.AuditStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PremiumAuditResponse(
        UUID auditId,
        UUID policyId,
        String auditNumber,
        AuditType auditType,
        AuditStatus status,
        BigDecimal estimatedExposure,
        BigDecimal actualExposure,
        String exposureBasis,
        BigDecimal premiumDelta,
        BigDecimal additionalPremium,
        BigDecimal returnPremium,
        UUID auditedByUserId,
        LocalDateTime auditedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}