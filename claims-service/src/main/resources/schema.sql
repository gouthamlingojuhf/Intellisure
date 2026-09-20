CREATE TABLE IF NOT EXISTS claim (
    claim_id BINARY(16) NOT NULL,
    policy_id BINARY(16) NOT NULL,
    customer_id BINARY(16) NOT NULL,
    
    claim_number VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    incident_date DATE NOT NULL,
    reported_date DATE NOT NULL,
    
    description TEXT,
    estimated_loss DECIMAL(15,2),
    payout_amount DECIMAL(15,2),
    
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    
    PRIMARY KEY (claim_id),
    UNIQUE KEY uk_claim_number (claim_number),
    INDEX idx_claim_policy (policy_id),
    INDEX idx_claim_customer (customer_id)
);

CREATE TABLE IF NOT EXISTS claim_assessment (
    assessment_id BINARY(16) NOT NULL,
    claim_id BINARY(16) NOT NULL,
    assessor_id BINARY(16),
    assessment_date DATE,
    findings TEXT,
    damage_estimate DECIMAL(15,2),
    status VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (assessment_id),
    CONSTRAINT fk_claim_assessment_claim FOREIGN KEY (claim_id) REFERENCES claim(claim_id) ON DELETE CASCADE
);
