package com.intellisure.workflownotificationservice.dto;

import java.util.List;

public record WorkflowTaskListResponse(
        List<WorkflowTaskResponse> items,
        Integer page,
        Integer size,
        Long totalElements
) {}