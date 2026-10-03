package com.intellisure.quotepolicyservice.dto.response;

import com.intellisure.quotepolicyservice.enums.PolicyStatus;

import java.time.LocalDate;
import java.util.UUID;

public record PolicyStatusResponse(

        UUID policyId,

        String policyNumber,

        UUID customerId,

        PolicyStatus status,

        LocalDate startDate,

        LocalDate endDate,

        boolean activeOnRequestedDate,

        LocalDate requestedDate
) {
}