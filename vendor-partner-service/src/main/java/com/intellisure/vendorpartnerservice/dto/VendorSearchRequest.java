package com.intellisure.vendorpartnerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record VendorSearchRequest(
        String serviceType,
        String location,
        java.math.BigDecimal radiusKm,
        String capability,
        String availabilityStatus,
        Integer page,
        Integer size
) {}