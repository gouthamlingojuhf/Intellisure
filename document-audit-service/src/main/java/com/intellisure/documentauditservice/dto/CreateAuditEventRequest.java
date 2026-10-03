package com.intellisure.documentauditservice.dto;

import com.intellisure.documentauditservice.entity.AuditActor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAuditEventRequest(
        @NotBlank String serviceName,
        @NotNull UUID entityId,
        @NotBlank String entityType,
        @NotBlank String action,
        UUID userId,
        AuditActor actor,
        String reason,
        String ruleVersion,
        String modelVersion,
        UUID correlationId,
        String stateBefore,
        String stateAfter,
        String eventDetails,
        String ipAddress
) {}