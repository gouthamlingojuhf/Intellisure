package com.intellisure.vendorpartnerservice.dto;

import java.time.LocalDate;

public record AcceptAssignmentRequest(
        String acceptanceNote,
        LocalDate expectedStartDate
) {}