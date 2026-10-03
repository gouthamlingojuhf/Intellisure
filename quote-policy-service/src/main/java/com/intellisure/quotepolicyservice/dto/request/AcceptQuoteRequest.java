package com.intellisure.quotepolicyservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AcceptQuoteRequest(

        @NotNull(message = "Accepted-by user ID is required")
        UUID acceptedByUserId
) {
}