CREATE TABLE IF NOT EXISTS vendor (
    vendor_id BINARY(16) NOT NULL,
    vendor_name VARCHAR(255) NOT NULL,
    vendor_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    service_regions VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (vendor_id)
);

CREATE TABLE IF NOT EXISTS vendor_assignment (
    assignment_id BINARY(16) NOT NULL,
    vendor_id BINARY(16) NOT NULL,
    claim_id BINARY(16),
    service_requested TEXT,
    status VARCHAR(50) NOT NULL,
    assigned_date DATE,
    completed_date DATE,
    cost DECIMAL(15,2),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (assignment_id),
    CONSTRAINT fk_vendor_assignment_vendor FOREIGN KEY (vendor_id) REFERENCES vendor(vendor_id) ON DELETE CASCADE,
    INDEX idx_vendor_assignment_claim (claim_id)
);
