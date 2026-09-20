package com.intellisure.recoveryservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("recovery_case")
public class RecoveryCase implements Persistable<UUID> {

    @Id
    private UUID recoveryCaseId;

    private UUID claimId;
    private UUID customerId;

    private String severity;
    private String status;
    private String recoveryObjective;

    private LocalDate targetRestoreDate;
    private BigDecimal currentRestorePercent;
    
    private UUID ownerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return recoveryCaseId;
    }
}
