CREATE TABLE IF NOT EXISTS claim_intelligence_snapshot (
    snapshot_id BINARY(16) NOT NULL,
    claim_id BINARY(16) NOT NULL,
    priority VARCHAR(50),
    priority_score DECIMAL(5,2),
    fraud_risk_flag BOOLEAN,
    severity_band VARCHAR(50),
    key_signals TEXT,
    model_version VARCHAR(50),
    generated_at DATETIME NOT NULL,
    PRIMARY KEY (snapshot_id),
    INDEX idx_claim_intelligence (claim_id)
);

CREATE TABLE IF NOT EXISTS risk_score_snapshot (
    snapshot_id BINARY(16) NOT NULL,
    customer_id BINARY(16) NOT NULL,
    risk_score DECIMAL(5,2),
    risk_band VARCHAR(50),
    key_factors TEXT,
    model_version VARCHAR(50),
    generated_at DATETIME NOT NULL,
    PRIMARY KEY (snapshot_id),
    INDEX idx_risk_score_customer (customer_id)
);

CREATE TABLE IF NOT EXISTS renewal_intelligence_snapshot (
    snapshot_id BINARY(16) NOT NULL,
    policy_id BINARY(16) NOT NULL,
    renewal_risk_band VARCHAR(50),
    suggested_action VARCHAR(100),
    claim_trend VARCHAR(100),
    risk_trend VARCHAR(100),
    premium_change_indicator DECIMAL(5,2),
    key_factors TEXT,
    model_version VARCHAR(50),
    generated_at DATETIME NOT NULL,
    PRIMARY KEY (snapshot_id),
    INDEX idx_renewal_intelligence (policy_id)
);
