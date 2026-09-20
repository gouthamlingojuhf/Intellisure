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
@Table("document")
public class Document implements Persistable<UUID> {

    @Id
    private UUID documentId;

    private UUID entityId;
    private String entityType;
    private String documentType;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String storagePath;
    
    private UUID uploadedBy;
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
        return documentId;
    }
}
