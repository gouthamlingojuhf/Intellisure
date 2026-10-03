package com.intellisure.workflownotificationservice.dto;

import java.time.LocalDate;
import java.util.UUID;

public record TaskFilterRequest(
        UUID workflowId,
        UUID assigneeUserId,
        String taskType,
        String status,
        LocalDate fromDate,
        LocalDate toDate,
        Integer page,
        Integer size
) {}