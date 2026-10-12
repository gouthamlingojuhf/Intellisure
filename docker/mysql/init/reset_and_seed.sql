-- ==============================================================================
-- IntelliSure Database Reset & Seed Script
-- Purpose:
--   1. Clear all transactional data (quotes, policies, claims, recovery, chat, audit, analytics).
--   2. Clear all user accounts and customer profiles.
--   3. Seed exactly 2 employees per role with credentials {role}{1/2}@intellisure.com / Password@123.
--   4. Seed 4 accredited network vendors across different service domains.
--   5. Leave policyholder profiles empty (to be registered fresh during testing).
--   6. Retain system configuration and risk rule definitions.
-- ==============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------------------
-- 1. CUSTOMER PARTY SERVICE (customer_party_db)
-- ------------------------------------------------------------------------------
USE customer_party_db;

DELETE FROM user_role_audit;
DELETE FROM business_customer;
DELETE FROM user_account;

-- Password@123 BCrypt Hash: $2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq

-- UNDERWRITER (2 Accounts)
INSERT INTO user_account (user_id, email, password_hash, role, account_status, display_name, created_at, updated_at)
VALUES 
  (UNHEX(REPLACE('00000001-0000-0000-0000-000000000001', '-', '')), 'underwriter1@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'UNDERWRITER', 'ACTIVE', 'Underwriter One', NOW(), NOW()),
  (UNHEX(REPLACE('00000001-0000-0000-0000-000000000002', '-', '')), 'underwriter2@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'UNDERWRITER', 'ACTIVE', 'Underwriter Two', NOW(), NOW());

-- RISK ENGINEER (2 Accounts)
INSERT INTO user_account (user_id, email, password_hash, role, account_status, display_name, created_at, updated_at)
VALUES 
  (UNHEX(REPLACE('00000002-0000-0000-0000-000000000001', '-', '')), 'riskengineer1@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'RISK_ENGINEER', 'ACTIVE', 'Risk Engineer One', NOW(), NOW()),
  (UNHEX(REPLACE('00000002-0000-0000-0000-000000000002', '-', '')), 'riskengineer2@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'RISK_ENGINEER', 'ACTIVE', 'Risk Engineer Two', NOW(), NOW());

-- CLAIMS ADJUSTER (2 Accounts)
INSERT INTO user_account (user_id, email, password_hash, role, account_status, display_name, created_at, updated_at)
VALUES 
  (UNHEX(REPLACE('00000003-0000-0000-0000-000000000001', '-', '')), 'claimsadjuster1@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'CLAIMS_ADJUSTER', 'ACTIVE', 'Claims Adjuster One', NOW(), NOW()),
  (UNHEX(REPLACE('00000003-0000-0000-0000-000000000002', '-', '')), 'claimsadjuster2@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'CLAIMS_ADJUSTER', 'ACTIVE', 'Claims Adjuster Two', NOW(), NOW());

-- CLAIMS MANAGER (2 Accounts)
INSERT INTO user_account (user_id, email, password_hash, role, account_status, display_name, created_at, updated_at)
VALUES 
  (UNHEX(REPLACE('00000004-0000-0000-0000-000000000001', '-', '')), 'claimsmanager1@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'CLAIMS_MANAGER', 'ACTIVE', 'Claims Manager One', NOW(), NOW()),
  (UNHEX(REPLACE('00000004-0000-0000-0000-000000000002', '-', '')), 'claimsmanager2@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'CLAIMS_MANAGER', 'ACTIVE', 'Claims Manager Two', NOW(), NOW());

-- VENDOR MANAGER (2 Accounts)
INSERT INTO user_account (user_id, email, password_hash, role, account_status, display_name, created_at, updated_at)
VALUES 
  (UNHEX(REPLACE('00000005-0000-0000-0000-000000000001', '-', '')), 'vendormanager1@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'VENDOR_MANAGER', 'ACTIVE', 'Vendor Manager One', NOW(), NOW()),
  (UNHEX(REPLACE('00000005-0000-0000-0000-000000000002', '-', '')), 'vendormanager2@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'VENDOR_MANAGER', 'ACTIVE', 'Vendor Manager Two', NOW(), NOW());

-- SYSTEM ADMINISTRATOR (2 Accounts)
INSERT INTO user_account (user_id, email, password_hash, role, account_status, display_name, created_at, updated_at)
VALUES 
  (UNHEX(REPLACE('00000006-0000-0000-0000-000000000001', '-', '')), 'admin1@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'SYSTEM_ADMINISTRATOR', 'ACTIVE', 'Admin One', NOW(), NOW()),
  (UNHEX(REPLACE('00000006-0000-0000-0000-000000000002', '-', '')), 'admin2@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'SYSTEM_ADMINISTRATOR', 'ACTIVE', 'Admin Two', NOW(), NOW()),
  (UNHEX(REPLACE('00000006-0000-0000-0000-000000000000', '-', '')), 'admin@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'SYSTEM_ADMINISTRATOR', 'ACTIVE', 'IntelliSure Admin', NOW(), NOW());

-- VENDOR PARTNER (2 User Accounts)
INSERT INTO user_account (user_id, email, password_hash, role, account_status, display_name, created_at, updated_at)
VALUES 
  (UNHEX(REPLACE('00000007-0000-0000-0000-000000000001', '-', '')), 'vendor1@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'VENDOR', 'ACTIVE', 'Vendor Partner One', NOW(), NOW()),
  (UNHEX(REPLACE('00000007-0000-0000-0000-000000000002', '-', '')), 'vendor2@intellisure.com', '$2a$10$egBGhUXRaNzzm8/x2k76S.AZkOeI10ZNtX0QTg6XoMAyx.FxqhaRq', 'VENDOR', 'ACTIVE', 'Vendor Partner Two', NOW(), NOW());

-- ------------------------------------------------------------------------------
-- 2. VENDOR PARTNER SERVICE (vendor_partner_db)
-- ------------------------------------------------------------------------------
USE vendor_partner_db;

DELETE FROM vendor_performance;
DELETE FROM vendor_assignment;
DELETE FROM vendor_onboarding_request;
DELETE FROM vendor;

-- Seed 4 accredited network vendors across 4 different domains
INSERT INTO vendor (
  vendor_id, legal_name, display_name, vendor_type,
  service_types, capabilities, service_areas,
  contact_name, contact_phone, contact_email,
  verification_status, active_status, created_at, updated_at
) VALUES 
(
  UNHEX(REPLACE('10000000-0000-0000-0000-000000000001', '-', '')),
  'Apex Commercial Restoration LLC',
  'Apex Property Restoration',
  'PROPERTY_RESTORATION',
  JSON_ARRAY('RESTORATION', 'STRUCTURAL_DRYING', 'RECONSTRUCTION'),
  JSON_ARRAY('COMMERCIAL_REBUILD', 'SOOT_ABATEMENT', 'RAPID_MOBILIZATION'),
  JSON_ARRAY('NATIONAL', 'TRI_STATE', 'NORTHEAST'),
  'Marcus Vance',
  '+1-800-555-0191',
  'apex.restoration@vendor-network.intellisure.com',
  'VERIFIED',
  'ACTIVE',
  NOW(),
  NOW()
),
(
  UNHEX(REPLACE('10000000-0000-0000-0000-000000000002', '-', '')),
  'AquaGuard Water Mitigation Corp',
  'AquaGuard Mitigation Services',
  'CLEANUP_REMEDIATION',
  JSON_ARRAY('WATER_EXTRACTION', 'MOLD_REMEDIATION', 'DEHUMIDIFICATION'),
  JSON_ARRAY('HIGH_VOLUME_PUMPING', 'THERMAL_IMAGING', 'MICROBIAL_BARRIER'),
  JSON_ARRAY('NATIONAL', 'SOUTHEAST', 'MIDWEST'),
  'Sarah Jenkins',
  '+1-800-555-0192',
  'aquaguard@vendor-network.intellisure.com',
  'VERIFIED',
  'ACTIVE',
  NOW(),
  NOW()
),
(
  UNHEX(REPLACE('10000000-0000-0000-0000-000000000003', '-', '')),
  'VoltPro Commercial Electrical Inc',
  'VoltPro Electrical Solutions',
  'OTHER',
  JSON_ARRAY('ELECTRICAL_REPAIR', 'SWITCHGEAR_MAINTENANCE', 'SAFETY_INSPECTION'),
  JSON_ARRAY('EMERGENCY_POWER', 'PANEL_CERTIFICATION', 'HIGH_VOLTAGE_REPAIR'),
  JSON_ARRAY('NATIONAL', 'WEST_COAST', 'NORTHWEST'),
  'David Cho',
  '+1-800-555-0193',
  'voltpro@vendor-network.intellisure.com',
  'VERIFIED',
  'ACTIVE',
  NOW(),
  NOW()
),
(
  UNHEX(REPLACE('10000000-0000-0000-0000-000000000004', '-', '')),
  'EquipDirect Logistics Group',
  'EquipDirect Industrial Rentals',
  'EQUIPMENT_RENTAL',
  JSON_ARRAY('GENERATOR_RENTAL', 'INDUSTRIAL_DRYING', 'MOBILE_POWER'),
  JSON_ARRAY('HEAVY_DESSICANT_DRYERS', 'DIESEL_GENERATORS', 'TEMPORARY_HVAC'),
  JSON_ARRAY('NATIONAL', 'CENTRAL', 'SOUTHWEST'),
  'Elena Rostova',
  '+1-800-555-0194',
  'equipdirect@vendor-network.intellisure.com',
  'VERIFIED',
  'ACTIVE',
  NOW(),
  NOW()
);

-- ------------------------------------------------------------------------------
-- 3. QUOTE & POLICY SERVICE (quote_policy_db)
-- ------------------------------------------------------------------------------
USE quote_policy_db;

DELETE FROM subjectivity;
DELETE FROM premium_audit;
DELETE FROM renewal_transaction;
DELETE FROM endorsement_coverage;
DELETE FROM endorsement;
DELETE FROM policy_coverage;
DELETE FROM policy;
DELETE FROM quote_version;
DELETE FROM underwriting_decision;
DELETE FROM quote_coverage;
DELETE FROM quote;

-- ------------------------------------------------------------------------------
-- 4. RISK & UNDERWRITING SERVICE (risk_underwriting_db)
-- ------------------------------------------------------------------------------
USE risk_underwriting_db;

DELETE FROM underwriting_decision;
DELETE FROM subjectivity;
DELETE FROM underwriting_referral;
DELETE FROM risk_recommendation;
DELETE FROM risk_finding;
DELETE FROM risk_assessment;
-- Note: risk_rule is preserved for business rule evaluations

-- ------------------------------------------------------------------------------
-- 5. CLAIMS SERVICE (claims_db)
-- ------------------------------------------------------------------------------
USE claims_db;

DELETE FROM subrogation;
DELETE FROM salvage;
DELETE FROM coverage_decision;
DELETE FROM payment;
DELETE FROM claim_financials;
DELETE FROM business_income;
DELETE FROM claim_assessment;
DELETE FROM claim;

-- ------------------------------------------------------------------------------
-- 6. RECOVERY & CONTINUITY SERVICE (recovery_continuity_db)
-- ------------------------------------------------------------------------------
USE recovery_continuity_db;

DELETE FROM recovery_support_request;
DELETE FROM recovery_plan;
DELETE FROM recovery_case;

-- ------------------------------------------------------------------------------
-- 7. WORKFLOW & NOTIFICATION SERVICE (workflow_notification_db)
-- ------------------------------------------------------------------------------
USE workflow_notification_db;

DELETE FROM chat_message;
DELETE FROM chat_participant;
DELETE FROM chat_channel;
DELETE FROM notification;
DELETE FROM workflow_task;
DELETE FROM workflow;

-- ------------------------------------------------------------------------------
-- 8. DOCUMENT & AUDIT SERVICE (document_audit_db)
-- ------------------------------------------------------------------------------
USE document_audit_db;

DELETE FROM document;
DELETE FROM audit_event;

-- ------------------------------------------------------------------------------
-- 9. ANALYTICS & INTELLIGENCE SERVICE (analytics_intelligence_db)
-- ------------------------------------------------------------------------------
USE analytics_intelligence_db;

DELETE FROM executive_dashboard_summary;
DELETE FROM loss_triangle;
DELETE FROM loss_ratio_metrics;
DELETE FROM renewal_intelligence_snapshot;
DELETE FROM risk_score_snapshot;
DELETE FROM claim_intelligence_snapshot;

SET FOREIGN_KEY_CHECKS = 1;
