package com.intellisure.workflownotificationservice.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MarkNotificationReadRequest(
        @NotNull UUID notificationId
) {}