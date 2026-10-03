package com.intellisure.vendorpartnerservice.dto;

import jakarta.validation.constraints.NotBlank;

public record DeclineAssignmentRequest(
        @NotBlank String declineReason
) {}