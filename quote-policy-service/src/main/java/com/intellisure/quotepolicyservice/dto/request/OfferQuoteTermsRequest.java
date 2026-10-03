package com.intellisure.quotepolicyservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record OfferQuoteTermsRequest(

        @NotNull(message = "Quote expiration date and time are required")
        @Future(
                message = "Quote expiration date and time must be in the future"
        )
        LocalDateTime quoteExpiresAt,

        @NotEmpty(
                message = "At least one offered coverage is required"
        )
        List<@Valid OfferedCoverageRequest> coverages
) {
}