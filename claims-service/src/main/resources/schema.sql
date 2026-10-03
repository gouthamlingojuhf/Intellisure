CREATE TABLE IF NOT EXISTS claim (
    claim_id BINARY(16) NOT NULL,
    policy_id BINARY(16) NOT NULL,
    customer_id BINARY(16) NOT NULL,
    policy_number VARCHAR(100),

    claim_number VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    incident_type VARCHAR(100),
    incident_description TEXT,
    incident_date DATE NOT NULL,
    reported_date DATE NOT NULL,
    incident_location VARCHAR(255),
    priority VARCHAR(50),
    assigned_adjuster_id BINARY(16),

    description TEXT,
    estimated_loss DECIMAL(15,2),
    estimated_covered_loss DECIMAL(15,2),
    coverage_confirmed BOOLEAN,
    coverage_decision VARCHAR(100),
    coverage_decision_reason TEXT,
    closure_reason VARCHAR(255),
    payout_amount DECIMAL(15,2),

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    PRIMARY KEY (claim_id),
    UNIQUE KEY uk_claim_number (claim_number),
    INDEX idx_claim_policy (policy_id),
    INDEX idx_claim_customer (customer_id),
    INDEX idx_claim_status (status),
    INDEX idx_claim_adjuster (assigned_adjuster_id)
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

CREATE TABLE IF NOT EXISTS claim_financials (
    financial_id BINARY(16) NOT NULL,
    claim_id BINARY(16) NOT NULL,
    reserve_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_incurred DECIMAL(15,2) NOT NULL DEFAULT 0,
    paid_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    outstanding_reserve DECIMAL(15,2) NOT NULL DEFAULT 0,
    last_updated_by BINARY(16),
    last_updated_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (financial_id),
    CONSTRAINT fk_claim_financials_claim FOREIGN KEY (claim_id) REFERENCES claim(claim_id) ON DELETE CASCADE,
    UNIQUE KEY uk_claim_financials_claim (claim_id),
    INDEX idx_financials_claim (claim_id)
);

CREATE TABLE IF NOT EXISTS payment (
    payment_id BINARY(16) NOT NULL,
    claim_id BINARY(16) NOT NULL,
    payment_reference VARCHAR(100),
    amount DECIMAL(15,2) NOT NULL,
    payment_date DATETIME NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    reference_number VARCHAR(100),
    notes TEXT,
    created_by BINARY(16) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (payment_id),
    CONSTRAINT fk_payment_claim FOREIGN KEY (claim_id) REFERENCES claim(claim_id) ON DELETE CASCADE,
    INDEX idx_payment_claim (claim_id),
    INDEX idx_payment_status (status),
    INDEX idx_payment_date (payment_date)
);

CREATE TABLE IF NOT EXISTS coverage_decision (
    decision_id BINARY(16) NOT NULL,
    claim_id BINARY(16) NOT NULL,
    coverage_code VARCHAR(100) NOT NULL,
    decision VARCHAR(50) NOT NULL,
    decision_reason TEXT,
    decided_by BINARY(16) NOT NULL,
    decided_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (decision_id),
    CONSTRAINT fk_coverage_decision_claim FOREIGN KEY (claim_id) REFERENCES claim(claim_id) ON DELETE CASCADE,
    INDEX idx_coverage_decision_claim (claim_id)
);

CREATE TABLE IF NOT EXISTS salvage (
    salvage_id BINARY(16) NOT NULL,
    claim_id BINARY(16) NOT NULL,
    description TEXT,
    estimated_value DECIMAL(15,2),
    actual_value DECIMAL(15,2),
    status VARCHAR(50) NOT NULL,
    buyer VARCHAR(255),
    sale_date DATE,
    sale_amount DECIMAL(15,2),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (salvage_id),
    CONSTRAINT fk_salvage_claim FOREIGN KEY (claim_id) REFERENCES claim(claim_id) ON DELETE CASCADE,
    INDEX idx_salvage_claim (claim_id)
);

CREATE TABLE IF NOT EXISTS subrogation (
    subrogation_id BINARY(16) NOT NULL,
    claim_id BINARY(16) NOT NULL,
    third_party_name VARCHAR(255),
    third_party_insurance VARCHAR(255),
    amount_claimed DECIMAL(15,2),
    amount_recovered DECIMAL(15,2) DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (subrogation_id),
    CONSTRAINT fk_subrogation_claim FOREIGN KEY (claim_id) REFERENCES claim(claim_id) ON DELETE CASCADE,
    INDEX idx_subrogation_claim (claim_id)
);

CREATE TABLE IF NOT EXISTS business_income (
    business_income_id BINARY(16) NOT NULL,
    claim_id BINARY(16) NOT NULL,
    coverage_limit DECIMAL(15,2),
    waiting_period_days INTEGER,
    restoration_period_days INTEGER,
    period_of_indemnity_start DATE,
    period_of_indemnity_end DATE,
    estimated_loss DECIMAL(15,2),
    actual_loss DECIMAL(15,2),
    coverage_confirmed BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (business_income_id),
    CONSTRAINT fk_business_income_claim FOREIGN KEY (claim_id) REFERENCES claim(claim_id) ON DELETE CASCADE,
    UNIQUE KEY uk_business_income_claim (claim_id),
    INDEX idx_business_income_claim (claim_id)
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