package com.intellisure.vendorpartnerservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import java.time.LocalDateTime;

public record VendorResponse(
        UUID vendorId,
        String vendorName,
        String vendorType,
        String status,
        String contactEmail,
        String contactPhone,
        String serviceRegions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
