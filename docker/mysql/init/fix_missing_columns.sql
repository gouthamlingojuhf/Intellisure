-- ==============================================================================
-- IntelliSure Database Migration / Fix Script
-- Purpose: Safely adds any new columns (annual_turnover, employee_count, etc.)
--          to existing tables without losing existing data.
-- ==============================================================================

USE customer_party_db;

DELIMITER $$
DROP PROCEDURE IF EXISTS FixCustomerPartyColumns$$
CREATE PROCEDURE FixCustomerPartyColumns()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'customer_party_db' 
          AND TABLE_NAME = 'business_customer' 
          AND COLUMN_NAME = 'annual_turnover'
    ) THEN
        ALTER TABLE business_customer ADD COLUMN annual_turnover DECIMAL(15,2) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'customer_party_db' 
          AND TABLE_NAME = 'business_customer' 
          AND COLUMN_NAME = 'employee_count'
    ) THEN
        ALTER TABLE business_customer ADD COLUMN employee_count INT NULL;
    END IF;
END$$
DELIMITER ;

CALL FixCustomerPartyColumns();
DROP PROCEDURE FixCustomerPartyColumns;

-- ------------------------------------------------------------------------------
-- claims_db: ensure adjuster review and policy metadata columns exist
-- ------------------------------------------------------------------------------
USE claims_db;

DELIMITER $$
DROP PROCEDURE IF EXISTS FixClaimsColumns$$
CREATE PROCEDURE FixClaimsColumns()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'claims_db' 
          AND TABLE_NAME = 'claim' 
          AND COLUMN_NAME = 'policy_number'
    ) THEN
        ALTER TABLE claim ADD COLUMN policy_number VARCHAR(100) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'claims_db' 
          AND TABLE_NAME = 'claim' 
          AND COLUMN_NAME = 'incident_description'
    ) THEN
        ALTER TABLE claim ADD COLUMN incident_description TEXT NULL;
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'claims_db' 
          AND TABLE_NAME = 'claim' 
          AND COLUMN_NAME = 'estimated_covered_loss'
    ) THEN
        ALTER TABLE claim ADD COLUMN estimated_covered_loss DECIMAL(15,2) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'claims_db' 
          AND TABLE_NAME = 'claim' 
          AND COLUMN_NAME = 'coverage_decision_reason'
    ) THEN
        ALTER TABLE claim ADD COLUMN coverage_decision_reason TEXT NULL;
    END IF;
END$$
DELIMITER ;

CALL FixClaimsColumns();
DROP PROCEDURE FixClaimsColumns;
