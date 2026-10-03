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

CREATE TABLE IF NOT EXISTS loss_ratio_metrics (
    metrics_id BINARY(16) NOT NULL,
    total_earned_premium DECIMAL(20,2) NOT NULL,
    total_incurred_claims DECIMAL(20,2) NOT NULL,
    loss_adjustment_expenses DECIMAL(20,2) NOT NULL DEFAULT 0,
    loss_ratio_percentage DECIMAL(10,2) NOT NULL,
    active_policy_count INTEGER NOT NULL,
    total_claims_filed INTEGER NOT NULL,
    calculated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    period_start DATETIME,
    period_end DATETIME,
    PRIMARY KEY (metrics_id),
    INDEX idx_loss_ratio_calculated (calculated_at)
);

CREATE TABLE IF NOT EXISTS loss_triangle (
    triangle_id BINARY(16) NOT NULL,
    accident_year INTEGER NOT NULL,
    development_year INTEGER NOT NULL,
    cumulative_incurred_claims DECIMAL(20,2) NOT NULL,
    cumulative_paid_claims DECIMAL(20,2) NOT NULL,
    case_reserves DECIMAL(20,2) NOT NULL DEFAULT 0,
    ibnr_reserves DECIMAL(20,2) NOT NULL DEFAULT 0,
    calculated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (triangle_id),
    UNIQUE KEY uk_loss_triangle_year (accident_year, development_year),
    INDEX idx_loss_triangle_accident_year (accident_year),
    INDEX idx_loss_triangle_development_year (development_year)
);

CREATE TABLE IF NOT EXISTS executive_dashboard_summary (
    summary_id BINARY(16) NOT NULL,
    total_written_premium DECIMAL(20,2) NOT NULL,
    total_earned_premium DECIMAL(20,2) NOT NULL,
    total_incurred_losses DECIMAL(20,2) NOT NULL,
    loss_ratio_percentage DECIMAL(10,2) NOT NULL,
    claims_frequency DECIMAL(10,2) NOT NULL,
    net_subrogation_yield DECIMAL(20,2) NOT NULL,
    active_policy_count INTEGER NOT NULL,
    total_claims_filed INTEGER NOT NULL,
    open_claims_count INTEGER NOT NULL,
    closed_claims_count INTEGER NOT NULL,
    calculated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    period_start DATETIME,
    period_end DATETIME,
    PRIMARY KEY (summary_id),
    INDEX idx_dashboard_calculated (calculated_at)
);