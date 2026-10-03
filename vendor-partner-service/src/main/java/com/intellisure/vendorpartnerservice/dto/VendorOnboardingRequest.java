package com.intellisure.vendorpartnerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record VendorOnboardingRequest(
        @NotBlank String legalName,
        @NotBlank String displayName,
        @NotBlank String vendorType,
        @NotNull List<String> serviceTypes,
        @NotNull List<String> capabilities,
        @NotNull List<String> serviceAreas,
        @NotBlank String contactName,
        @NotBlank String contactPhone,
        @NotBlank String contactEmail,
        List<UUID> documentIds
) {}