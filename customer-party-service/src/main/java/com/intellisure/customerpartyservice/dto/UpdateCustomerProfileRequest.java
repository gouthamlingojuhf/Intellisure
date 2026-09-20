package com.intellisure.customerpartyservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCustomerProfileRequest(
        @NotBlank String businessName,
        @NotBlank String ownerName,
        String phone,
        String businessType,
        String address,
        String city,
        String state,
        String country,
        String postalCode
) {}
