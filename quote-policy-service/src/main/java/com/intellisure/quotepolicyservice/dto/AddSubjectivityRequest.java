package com.intellisure.quotepolicyservice.dto;

import jakarta.validation.constraints.NotBlank;

public record AddSubjectivityRequest(
        @NotBlank String subjectivityCode,
        @NotBlank String description
) {}