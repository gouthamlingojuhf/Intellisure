package com.intellisure.vendorpartnerservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateAssignmentStatusRequest(
        @NotBlank String status,
        String progressNote,
        LocalDate completionDate,
        List<UUID> evidenceDocumentIds
) {}