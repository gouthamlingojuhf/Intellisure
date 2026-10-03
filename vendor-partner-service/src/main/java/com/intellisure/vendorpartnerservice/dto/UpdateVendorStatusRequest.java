package com.intellisure.vendorpartnerservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateVendorStatusRequest(
        @NotBlank String activeStatus,
        String statusReason
) {}