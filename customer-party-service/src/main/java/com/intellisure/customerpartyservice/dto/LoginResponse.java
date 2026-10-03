package com.intellisure.customerpartyservice.dto;

import java.util.UUID;

public record LoginResponse(

        String accessToken,

        String tokenType,

        long expiresIn,

        UUID userId,

        UUID customerId,

        String role
) {
}