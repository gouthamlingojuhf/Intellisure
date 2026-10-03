package com.intellisure.customerpartyservice.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleAssignmentRequest(
        @NotBlank String role
) {}