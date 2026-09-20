package com.intellisure.vendorpartnerservice.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterVendorRequest(
        @NotBlank String vendorName,
        @NotBlank String vendorType,
        @NotBlank String contactEmail,
        @NotBlank String contactPhone,
        String serviceRegions
) {}
