package com.intellisure.riskunderwritingservice.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record SubmitSubjectivityRequest(

        @NotEmpty(
                message = "At least one evidence document ID is required"
        )
        List<UUID> evidenceDocumentIds
) {
}