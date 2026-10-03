package com.intellisure.quotepolicyservice.dto;

import jakarta.validation.constraints.NotBlank;

public record RenewalSubjectivityRequest(
        @NotBlank String subjectivityCode,
        @NotBlank String description
) {}