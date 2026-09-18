CREATE TABLE IF NOT EXISTS quote (
                                     quote_id BINARY(16) NOT NULL,
    customer_id BINARY(16) NOT NULL,

    business_name VARCHAR(255) NOT NULL,
    business_type VARCHAR(100) NOT NULL,

    annual_revenue DECIMAL(15,2) NOT NULL,
    employee_count INT NOT NULL,

    requested_coverage_amount DECIMAL(15,2) NOT NULL,

    estimated_premium DECIMAL(15,2),

    quote_status VARCHAR(50) NOT NULL,

    valid_until DATETIME,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    PRIMARY KEY (quote_id),

    INDEX idx_quote_customer (customer_id),
    INDEX idx_quote_status (quote_status)
    );


CREATE TABLE IF NOT EXISTS quote_coverage (
                                              quote_coverage_id BINARY(16) NOT NULL,
    quote_id BINARY(16) NOT NULL,

    coverage_type VARCHAR(100) NOT NULL,
    coverage_limit DECIMAL(15,2) NOT NULL,
    deductible DECIMAL(15,2) NOT NULL,

    created_at DATETIME NOT NULL,

    PRIMARY KEY (quote_coverage_id),

    INDEX idx_quote_coverage_quote (quote_id),

    CONSTRAINT fk_quote_coverage_quote
    FOREIGN KEY (quote_id)
    REFERENCES quote(quote_id)
    ON DELETE CASCADE
    );


CREATE TABLE IF NOT EXISTS policy (
                                      policy_id BINARY(16) NOT NULL,
    quote_id BINARY(16) NOT NULL,
    customer_id BINARY(16) NOT NULL,

    policy_number VARCHAR(50) NOT NULL,

    policy_status VARCHAR(50) NOT NULL,

    effective_date DATE NOT NULL,
    expiry_date DATE NOT NULL,

    total_premium DECIMAL(15,2) NOT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    PRIMARY KEY (policy_id),

    UNIQUE KEY uk_policy_number (policy_number),

    UNIQUE KEY uk_policy_quote (quote_id),

    INDEX idx_policy_customer (customer_id),
    INDEX idx_policy_status (policy_status),

    CONSTRAINT fk_policy_quote
    FOREIGN KEY (quote_id)
    REFERENCES quote(quote_id)
    );


CREATE TABLE IF NOT EXISTS policy_coverage (
                                               policy_coverage_id BINARY(16) NOT NULL,
    policy_id BINARY(16) NOT NULL,

    coverage_type VARCHAR(100) NOT NULL,
    coverage_limit DECIMAL(15,2) NOT NULL,
    deductible DECIMAL(15,2) NOT NULL,

    created_at DATETIME NOT NULL,

    PRIMARY KEY (policy_coverage_id),

    INDEX idx_policy_coverage_policy (policy_id),

    CONSTRAINT fk_policy_coverage_policy
    FOREIGN KEY (policy_id)
    REFERENCES policy(policy_id)
    ON DELETE CASCADE
    );
