package com.intellisure.vendorpartnerservice.entity;

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
@Table("vendor_assignment")
public class VendorAssignment implements Persistable<UUID> {

    @Id
    private UUID assignmentId;

    private UUID vendorId;
    private UUID claimId;

    private String serviceRequested;
    private String status;
    private LocalDate assignedDate;
    private LocalDate completedDate;
    private BigDecimal cost;

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
        return assignmentId;
    }
}
