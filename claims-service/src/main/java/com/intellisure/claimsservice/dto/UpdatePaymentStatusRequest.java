package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpdatePaymentStatusRequest(
        @NotBlank String status
) {}