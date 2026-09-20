package com.intellisure.vendorpartnerservice.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAssignmentRequest(@NotNull UUID vendorId, UUID claimId, String serviceRequested) {}
