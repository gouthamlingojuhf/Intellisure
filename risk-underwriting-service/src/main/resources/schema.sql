CREATE TABLE IF NOT EXISTS risk_assessment (
    assessment_id BINARY(16) NOT NULL,
    quote_id BINARY(16),
    policy_id BINARY(16),
    customer_id BINARY(16) NOT NULL,
    
    assessment_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    assessment_date DATE,
    
    location VARCHAR(255),
    business_operations TEXT,
    risk_score DECIMAL(5,2),
    summary TEXT,
    
    created_by BINARY(16),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    
    PRIMARY KEY (assessment_id),
    INDEX idx_risk_assessment_quote (quote_id),
    INDEX idx_risk_assessment_policy (policy_id),
    INDEX idx_risk_assessment_customer (customer_id)
);

CREATE TABLE IF NOT EXISTS risk_finding (
    finding_id BINARY(16) NOT NULL,
    assessment_id BINARY(16) NOT NULL,
    finding_type VARCHAR(100) NOT NULL,
    description TEXT,
    severity VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (finding_id),
    CONSTRAINT fk_risk_finding_assessment FOREIGN KEY (assessment_id) REFERENCES risk_assessment(assessment_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS risk_recommendation (
    recommendation_id BINARY(16) NOT NULL,
    assessment_id BINARY(16) NOT NULL,
    recommendation_type VARCHAR(100) NOT NULL,
    description TEXT,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    target_date DATE,
    verified_at DATETIME,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (recommendation_id),
    CONSTRAINT fk_risk_recommendation_assessment FOREIGN KEY (assessment_id) REFERENCES risk_assessment(assessment_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS risk_rule (
    rule_id BINARY(16) NOT NULL,
    rule_code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    condition_expression TEXT NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    priority INT,
    status VARCHAR(50) NOT NULL,
    effective_from DATE,
    effective_to DATE,
    version INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (rule_id),
    UNIQUE KEY uk_risk_rule_code_version (rule_code, version)
);
