CREATE TABLE IF NOT EXISTS recovery_case (
    recovery_case_id BINARY(16) NOT NULL,
    claim_id BINARY(16) NOT NULL,
    customer_id BINARY(16) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    recovery_objective TEXT,
    target_restore_date DATE,
    current_restore_percent DECIMAL(5,2),
    owner_id BINARY(16),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (recovery_case_id),
    UNIQUE KEY uk_recovery_case_claim (claim_id),
    INDEX idx_recovery_case_customer (customer_id)
);

CREATE TABLE IF NOT EXISTS recovery_plan (
    recovery_plan_id BINARY(16) NOT NULL,
    recovery_case_id BINARY(16) NOT NULL,
    plan_summary TEXT,
    target_milestones TEXT,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (recovery_plan_id),
    CONSTRAINT fk_recovery_plan_case FOREIGN KEY (recovery_case_id) REFERENCES recovery_case(recovery_case_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recovery_support_request (
    support_request_id BINARY(16) NOT NULL,
    recovery_case_id BINARY(16) NOT NULL,
    support_type VARCHAR(100) NOT NULL,
    description TEXT,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    required_by_date DATE,
    location VARCHAR(255),
    vendor_assignment_id BINARY(16),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (support_request_id),
    CONSTRAINT fk_recovery_request_case FOREIGN KEY (recovery_case_id) REFERENCES recovery_case(recovery_case_id) ON DELETE CASCADE
);
