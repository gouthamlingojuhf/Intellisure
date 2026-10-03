package com.intellisure.vendorpartnerservice.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateVendorRequest(
        String displayName,
        @NotNull List<String> serviceTypes,
        @NotNull List<String> capabilities,
        @NotNull List<String> serviceAreas,
        String contactName,
        String contactPhone,
        String contactEmail
) {}