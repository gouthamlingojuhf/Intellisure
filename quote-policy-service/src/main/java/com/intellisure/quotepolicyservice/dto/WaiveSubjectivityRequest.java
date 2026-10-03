package com.intellisure.quotepolicyservice.dto;

import java.util.UUID;

public record WaiveSubjectivityRequest(
        UUID waivedByUserId,
        String reason
) {}