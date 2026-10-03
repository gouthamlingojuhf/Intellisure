package com.intellisure.customerpartyservice.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerProfileChangedEvent(
        UUID customerId,
        UUID userId,
        String changeType,
        String fieldName,
        String oldValue,
        String newValue,
        Long newVersion,
        LocalDateTime changedAt
) {}