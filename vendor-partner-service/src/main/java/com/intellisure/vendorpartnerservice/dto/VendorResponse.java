package com.intellisure.vendorpartnerservice.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record VendorResponse(
        UUID vendorId,
        String legalName,
        String displayName,
        String vendorType,
        List<String> serviceTypes,
        List<String> capabilities,
        List<String> serviceAreas,
        String verificationStatus,
        String activeStatus,
        String contactPhone,
        String contactEmail
) {}