package com.intellisure.vendorpartnerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record VerifyVendorRequest(
        @NotBlank String verificationDecision,
        String verificationNote,
        List<UUID> verifiedDocumentIds
) {}