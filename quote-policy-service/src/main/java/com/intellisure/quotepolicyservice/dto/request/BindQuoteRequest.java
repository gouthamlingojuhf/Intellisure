package com.intellisure.quotepolicyservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BindQuoteRequest(

        @NotNull(message = "Binding user ID is required")
        UUID boundByUserId
) {
}