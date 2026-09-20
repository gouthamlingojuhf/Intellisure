package com.intellisure.documentauditservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateAuditEventRequest(@NotBlank String serviceName, UUID entityId,
                                      @NotBlank String entityType, @NotBlank String action,
                                      UUID userId, String eventDetails, String ipAddress) {}
