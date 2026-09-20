CREATE TABLE IF NOT EXISTS document (
    document_id BINARY(16) NOT NULL,
    entity_id BINARY(16) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    document_type VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    storage_path VARCHAR(255) NOT NULL,
    uploaded_by BINARY(16),
    created_at DATETIME NOT NULL,
    PRIMARY KEY (document_id),
    INDEX idx_document_entity (entity_id, entity_type)
);

CREATE TABLE IF NOT EXISTS audit_event (
    audit_event_id BINARY(16) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    entity_id BINARY(16) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    user_id BINARY(16),
    event_details TEXT,
    ip_address VARCHAR(50),
    created_at DATETIME NOT NULL,
    PRIMARY KEY (audit_event_id),
    INDEX idx_audit_event_entity (entity_id, entity_type)
);
