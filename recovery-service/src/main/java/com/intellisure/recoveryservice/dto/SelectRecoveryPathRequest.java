package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.RecoveryPath;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record SelectRecoveryPathRequest(
        @NotNull RecoveryPath recoveryPath,
        UUID vendorId,               // Optional: only used if NETWORK_VENDOR chosen
        String taskDescription,       // Optional
        LocalDate dueDate,            // Optional
        String notes
) {}
