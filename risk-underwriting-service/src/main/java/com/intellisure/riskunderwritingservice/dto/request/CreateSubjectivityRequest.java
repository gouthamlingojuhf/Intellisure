package com.intellisure.riskunderwritingservice.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateSubjectivityRequest(

        @NotBlank(message = "Subjectivity type is required")
        @Size(
                max = 100,
                message = "Subjectivity type must not exceed 100 characters"
        )
        String subjectivityType,

        @NotBlank(message = "Subjectivity description is required")
        @Size(
                max = 5000,
                message = "Subjectivity description must not exceed 5000 characters"
        )
        String description,

        @NotNull(message = "Required-before-bind value is required")
        Boolean requiredBeforeBind,

        @FutureOrPresent(
                message = "Due date cannot be in the past"
        )
        LocalDate dueDate,

        @NotNull(message = "Created-by user ID is required")
        UUID createdBy
) {
}