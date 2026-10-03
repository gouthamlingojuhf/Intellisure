package com.intellisure.workflownotificationservice.dto;

import java.util.List;

public record NotificationListResponse(
        List<NotificationResponse> items,
        Integer page,
        Integer size,
        Long totalElements
) {}