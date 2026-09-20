package com.intellisure.documentauditservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("audit_event")
public class AuditEvent implements Persistable<UUID> {

    @Id
    private UUID auditEventId;

    private String serviceName;
    private UUID entityId;
    private String entityType;
    private String action;
    
    private UUID userId;
    private String eventDetails;
    private String ipAddress;

    private LocalDateTime createdAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return auditEventId;
    }
}
