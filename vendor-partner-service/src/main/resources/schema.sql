CREATE TABLE IF NOT EXISTS vendor (
    vendor_id BINARY(16) NOT NULL,
    legal_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    vendor_type VARCHAR(100) NOT NULL,
    service_types JSON NOT NULL,
    capabilities JSON NOT NULL,
    service_areas JSON NOT NULL,
    contact_name VARCHAR(255),
    contact_phone VARCHAR(50),
    contact_email VARCHAR(255),
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    active_status VARCHAR(50) NOT NULL DEFAULT 'INACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (vendor_id),
    INDEX idx_vendor_type (vendor_type),
    INDEX idx_vendor_active_status (active_status),
    INDEX idx_vendor_verification_status (verification_status)
);

CREATE TABLE IF NOT EXISTS vendor_onboarding_request (
    onboarding_request_id BINARY(16) NOT NULL,
    vendor_id BINARY(16) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME,
    reviewer_id BINARY(16),
    rejection_reason VARCHAR(255),
    PRIMARY KEY (onboarding_request_id),
    CONSTRAINT fk_onboarding_vendor FOREIGN KEY (vendor_id) REFERENCES vendor(vendor_id) ON DELETE CASCADE,
    INDEX idx_onboarding_vendor (vendor_id),
    INDEX idx_onboarding_status (status)
);

CREATE TABLE IF NOT EXISTS vendor_assignment (
    assignment_id BINARY(16) NOT NULL,
    vendor_id BINARY(16) NOT NULL,
    assignment_type VARCHAR(100) NOT NULL,
    claim_id BINARY(16),
    recovery_case_id BINARY(16),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    task_description VARCHAR(255),
    due_date DATE,
    priority VARCHAR(50),
    accepted_at DATETIME,
    completed_at DATETIME,
    evidence_document_ids JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (assignment_id),
    CONSTRAINT fk_assignment_vendor FOREIGN KEY (vendor_id) REFERENCES vendor(vendor_id) ON DELETE CASCADE,
    INDEX idx_assignment_vendor (vendor_id),
    INDEX idx_assignment_claim (claim_id),
    INDEX idx_assignment_recovery_case (recovery_case_id),
    INDEX idx_assignment_status (status),
    INDEX idx_assignment_type (assignment_type)
);

CREATE TABLE IF NOT EXISTS vendor_performance (
    performance_id BINARY(16) NOT NULL,
    vendor_id BINARY(16) NOT NULL,
    assignment_id BINARY(16) NOT NULL,
    quality_score DECIMAL(15,2) NOT NULL,
    timeliness_score DECIMAL(15,2) NOT NULL,
    communication_score DECIMAL(15,2) NOT NULL,
    outcome_score DECIMAL(15,2) NOT NULL,
    overall_score DECIMAL(15,2) NOT NULL,
    note VARCHAR(255),
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (performance_id),
    CONSTRAINT fk_performance_vendor FOREIGN KEY (vendor_id) REFERENCES vendor(vendor_id) ON DELETE CASCADE,
    CONSTRAINT fk_performance_assignment FOREIGN KEY (assignment_id) REFERENCES vendor_assignment(assignment_id) ON DELETE CASCADE,
    INDEX idx_performance_vendor (vendor_id),
    INDEX idx_performance_assignment (assignment_id),
    INDEX idx_performance_recorded (recorded_at)
);