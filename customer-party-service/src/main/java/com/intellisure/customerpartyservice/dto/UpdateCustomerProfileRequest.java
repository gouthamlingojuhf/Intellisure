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
        String postalCode,
        java.math.BigDecimal annualTurnover,
        Integer employeeCount
) {
    public UpdateCustomerProfileRequest(
            String businessName,
            String ownerName,
            String phone,
            String businessType,
            String address,
            String city,
            String state,
            String country,
            String postalCode
    ) {
        this(businessName, ownerName, phone, businessType, address, city, state, country, postalCode, null, null);
    }
}
