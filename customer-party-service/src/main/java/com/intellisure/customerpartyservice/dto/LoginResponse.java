package com.intellisure.customerpartyservice.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
)
{
}
