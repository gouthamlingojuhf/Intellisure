package com.intellisure.customerpartyservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String displayName,
        @NotBlank String role,
        String businessName,
        String ownerName,
        String phone,
        String businessType,
        String address,
        String city,
        String state,
        String country,
        String postalCode
) {}