package com.intellisure.workflownotificationservice.dto;

import java.util.List;

public record WorkflowListResponse(
        List<WorkflowResponse> items,
        Integer page,
        Integer size,
        Long totalElements
) {}