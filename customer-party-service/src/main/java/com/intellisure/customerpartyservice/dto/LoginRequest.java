package com.intellisure.customerpartyservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {
}
