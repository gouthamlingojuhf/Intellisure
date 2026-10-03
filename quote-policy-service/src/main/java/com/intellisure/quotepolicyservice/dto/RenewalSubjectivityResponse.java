package com.intellisure.quotepolicyservice.dto;

public record RenewalSubjectivityResponse(
        String subjectivityCode,
        String description,
        String status
) {}