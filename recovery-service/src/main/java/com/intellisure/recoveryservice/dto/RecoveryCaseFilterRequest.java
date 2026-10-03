package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
import java.time.LocalDate;
import java.util.UUID;

public record RecoveryCaseFilterRequest(
        UUID customerId,
        UUID ownerId,
        RecoveryCaseStatus status,
        RecoverySeverity severity,
        LocalDate fromDate,
        LocalDate toDate,
        Integer page,
        Integer size
) {}