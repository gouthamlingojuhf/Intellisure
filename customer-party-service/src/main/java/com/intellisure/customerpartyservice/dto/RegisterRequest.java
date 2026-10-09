package com.intellisure.customerpartyservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password,

        @NotBlank
        String displayName,

        String registrationType
) {
    public RegisterRequest(String email, String password, String displayName) {
        this(email, password, displayName, "POLICYHOLDER");
    }
}
