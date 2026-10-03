package com.intellisure.vendorpartnerservice.dto;

import java.time.LocalDate;
import java.util.UUID;

public record VendorAssignmentFilterRequest(
        UUID vendorId,
        UUID claimId,
        UUID recoveryCaseId,
        String assignmentType,
        String status,
        LocalDate fromDate,
        LocalDate toDate,
        Integer page,
        Integer size
) {}