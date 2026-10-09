CREATE TABLE IF NOT EXISTS risk_assessment (
                                               assessment_id CHAR(36) NOT NULL,
    assessment_number VARCHAR(50) NOT NULL,

    quote_id CHAR(36) NULL,
    policy_id CHAR(36) NULL,
    customer_id CHAR(36) NOT NULL,

    assessment_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    assessment_date DATE NOT NULL,

    location VARCHAR(500) NOT NULL,
    business_operations TEXT NOT NULL,

    annual_revenue DECIMAL(15, 2) NULL,
    annual_payroll DECIMAL(15, 2) NULL,
    employee_count INTEGER NULL,
    asset_value DECIMAL(15, 2) NULL,

    prior_claim_count INTEGER NOT NULL DEFAULT 0,
    prior_loss_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,

    risk_score DECIMAL(5, 2) NULL,
    risk_band VARCHAR(50) NULL,
    summary TEXT NULL,

    created_by CHAR(36) NOT NULL,
    assigned_underwriter_id CHAR(36) NULL,
    assigned_risk_engineer_id CHAR(36) NULL,

    submitted_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_risk_assessment
    PRIMARY KEY (assessment_id),

    CONSTRAINT uk_risk_assessment_number
    UNIQUE (assessment_number),

    CONSTRAINT chk_risk_assessment_reference
    CHECK (
              quote_id IS NOT NULL
              OR policy_id IS NOT NULL
          ),

    CONSTRAINT chk_risk_score_range
    CHECK (
              risk_score IS NULL
              OR (
              risk_score >= 0
              AND risk_score <= 100
                 )
    ),

    CONSTRAINT chk_employee_count
    CHECK (
              employee_count IS NULL
              OR employee_count >= 0
          ),

    CONSTRAINT chk_prior_claim_count
    CHECK (prior_claim_count >= 0),

    INDEX idx_assessment_quote_id (quote_id),
    INDEX idx_assessment_policy_id (policy_id),
    INDEX idx_assessment_customer_id (customer_id),
    INDEX idx_assessment_status (status),
    INDEX idx_assessment_underwriter (
                                         assigned_underwriter_id
                                     ),
    INDEX idx_assessment_risk_engineer (
                                           assigned_risk_engineer_id
                                       )
    );

CREATE TABLE IF NOT EXISTS risk_finding (
                                            finding_id CHAR(36) NOT NULL,
    assessment_id CHAR(36) NOT NULL,

    finding_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(50) NOT NULL,
    control_status VARCHAR(50) NOT NULL,

    evidence_document_ids JSON NULL,
    created_by CHAR(36) NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_risk_finding
    PRIMARY KEY (finding_id),

    CONSTRAINT fk_risk_finding_assessment
    FOREIGN KEY (assessment_id)
    REFERENCES risk_assessment (assessment_id)
    ON DELETE CASCADE,

    INDEX idx_finding_assessment_id (
                                        assessment_id
                                    ),

    INDEX idx_finding_severity (
                                   severity
                               ),

    INDEX idx_finding_type (
                               finding_type
                           )
    );

CREATE TABLE IF NOT EXISTS risk_recommendation (
                                                   recommendation_id CHAR(36) NOT NULL,
    assessment_id CHAR(36) NOT NULL,

    recommendation_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,

    required_before_bind BOOLEAN NOT NULL DEFAULT FALSE,

    target_date DATE NULL,
    completed_at DATETIME(6) NULL,
    verified_at DATETIME(6) NULL,
    verified_by CHAR(36) NULL,
    created_by CHAR(36) NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_risk_recommendation
    PRIMARY KEY (recommendation_id),

    CONSTRAINT fk_risk_recommendation_assessment
    FOREIGN KEY (assessment_id)
    REFERENCES risk_assessment (assessment_id)
    ON DELETE CASCADE,

    INDEX idx_recommendation_assessment_id (
                                               assessment_id
                                           ),

    INDEX idx_recommendation_status (
                                        status
                                    ),

    INDEX idx_recommendation_priority (
                                          priority
                                      )
    );

CREATE TABLE IF NOT EXISTS underwriting_referral (
                                                     referral_id CHAR(36) NOT NULL,
    assessment_id CHAR(36) NOT NULL,
    quote_id CHAR(36) NOT NULL,

    referral_reason TEXT NOT NULL,
    required_authority_level VARCHAR(100) NOT NULL,

    referred_by CHAR(36) NOT NULL,
    referred_to CHAR(36) NULL,

    status VARCHAR(50) NOT NULL,
    resolution_note TEXT NULL,

    created_at DATETIME(6) NOT NULL,
    resolved_at DATETIME(6) NULL,
    updated_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_underwriting_referral
    PRIMARY KEY (referral_id),

    CONSTRAINT fk_underwriting_referral_assessment
    FOREIGN KEY (assessment_id)
    REFERENCES risk_assessment (assessment_id)
    ON DELETE CASCADE,

    INDEX idx_referral_assessment_id (
                                         assessment_id
                                     ),

    INDEX idx_referral_quote_id (
                                    quote_id
                                ),

    INDEX idx_referral_status (
                                  status
                              ),

    INDEX idx_referral_referred_to (
                                       referred_to
                                   )
    );

CREATE TABLE IF NOT EXISTS subjectivity (
                                            subjectivity_id CHAR(36) NOT NULL,
    assessment_id CHAR(36) NOT NULL,
    quote_id CHAR(36) NOT NULL,

    subjectivity_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,

    required_before_bind BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(50) NOT NULL,

    due_date DATE NULL,
    satisfied_at DATETIME(6) NULL,
    verified_by CHAR(36) NULL,
    verification_note TEXT NULL,
    created_by CHAR(36) NOT NULL,

    evidence_document_ids JSON NULL,
    submitted_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_subjectivity
    PRIMARY KEY (subjectivity_id),

    CONSTRAINT fk_subjectivity_assessment
    FOREIGN KEY (assessment_id)
    REFERENCES risk_assessment (assessment_id)
    ON DELETE CASCADE,

    INDEX idx_subjectivity_assessment_id (
                                             assessment_id
                                         ),

    INDEX idx_subjectivity_quote_id (
                                        quote_id
                                    ),

    INDEX idx_subjectivity_status (
                                      status
                                  ),

    INDEX idx_subjectivity_required_before_bind (
                                                    required_before_bind
                                                )
    );

CREATE TABLE IF NOT EXISTS underwriting_decision (
                                                     decision_id CHAR(36) NOT NULL,
    assessment_id CHAR(36) NOT NULL,
    quote_id CHAR(36) NOT NULL,

    underwriter_id CHAR(36) NOT NULL,

    outcome VARCHAR(50) NOT NULL,
    decision_rationale TEXT NOT NULL,
    authority_level VARCHAR(100) NOT NULL,
    within_authority BOOLEAN NOT NULL,

    approved_limit DECIMAL(15, 2) NULL,
    approved_deductible DECIMAL(15, 2) NULL,
    indicated_premium DECIMAL(15, 2) NULL,

    conditions TEXT NULL,
    subjectivities_outstanding BOOLEAN NOT NULL DEFAULT FALSE,

    rule_version_reference VARCHAR(255) NULL,

    decided_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_underwriting_decision
    PRIMARY KEY (decision_id),

    CONSTRAINT fk_underwriting_decision_assessment
    FOREIGN KEY (assessment_id)
    REFERENCES risk_assessment (assessment_id)
    ON DELETE CASCADE,

    INDEX idx_decision_assessment_id (
                                         assessment_id
                                     ),

    INDEX idx_decision_quote_id (
                                    quote_id
                                ),

    INDEX idx_decision_underwriter_id (
                                          underwriter_id
                                      ),

    INDEX idx_decision_outcome (
                                   outcome
                               )
    );

CREATE TABLE IF NOT EXISTS risk_rule (
                                         rule_id CHAR(36) NOT NULL,
    rule_code VARCHAR(100) NOT NULL,

    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,

    condition_expression TEXT NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    action_value VARCHAR(500) NULL,

    priority INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,

    effective_from DATE NOT NULL,
    effective_to DATE NULL,

    version INTEGER NOT NULL DEFAULT 1,
    created_by CHAR(36) NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_risk_rule
    PRIMARY KEY (rule_id),

    CONSTRAINT uk_risk_rule_code_version
    UNIQUE (rule_code, version),

    CONSTRAINT chk_risk_rule_priority
    CHECK (priority >= 0),

    CONSTRAINT chk_risk_rule_version
    CHECK (version > 0),

    CONSTRAINT chk_risk_rule_dates
    CHECK (
              effective_to IS NULL
              OR effective_to >= effective_from
          ),

    INDEX idx_risk_rule_status (
                                   status
                               ),

    INDEX idx_risk_rule_effective_dates (
                                            effective_from,
                                            effective_to
                                        ),

    INDEX idx_risk_rule_priority (
                                     priority
                                 )
    );
