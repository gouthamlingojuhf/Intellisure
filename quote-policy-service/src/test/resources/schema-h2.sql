-- H2 flavoured schema used only by the test suite.
-- It mirrors src/main/resources/schema.sql (MySQL) but uses portable types
-- and explicitly quoted lower case identifiers, because the H2 R2DBC dialect
-- emits ANSI quoted identifiers.

CREATE TABLE IF NOT EXISTS "quote" (
    "quote_id"                 VARCHAR(36)    NOT NULL,
    "quote_number"             VARCHAR(50)    NOT NULL,
    "customer_id"              VARCHAR(36)    NOT NULL,
    "product_code"             VARCHAR(100)   NOT NULL,
    "insurance_need"           VARCHAR(20000) NOT NULL,
    "business_operations"      VARCHAR(20000) NOT NULL,
    "status"                   VARCHAR(50)    NOT NULL,
    "requested_effective_date" DATE           NOT NULL,
    "quote_expires_at"         TIMESTAMP(6)   NULL,
    "assigned_underwriter_id"  VARCHAR(36)    NULL,
    "risk_assessment_id"       VARCHAR(36)    NULL,
    "total_premium"            DECIMAL(15, 2) NULL,
    "submitted_at"             TIMESTAMP(6)   NULL,
    "quoted_at"                TIMESTAMP(6)   NULL,
    "accepted_by_user_id"      VARCHAR(36)    NULL,
    "accepted_at"              TIMESTAMP(6)   NULL,
    "bound_by_user_id"         VARCHAR(36)    NULL,
    "bound_at"                 TIMESTAMP(6)   NULL,
    "decline_reason"           VARCHAR(20000) NULL,
    "withdrawal_reason"        VARCHAR(20000) NULL,
    "version"                  BIGINT         NOT NULL DEFAULT 0,
    "subjectivities"           VARCHAR(20000) NULL,
    "created_at"               TIMESTAMP(6)   NOT NULL,
    "updated_at"               TIMESTAMP(6)   NOT NULL,
    CONSTRAINT "pk_quote" PRIMARY KEY ("quote_id"),
    CONSTRAINT "uk_quote_number" UNIQUE ("quote_number")
);

CREATE TABLE IF NOT EXISTS "quote_coverage" (
    "quote_coverage_id"    VARCHAR(36)    NOT NULL,
    "quote_id"             VARCHAR(36)    NOT NULL,
    "coverage_code"        VARCHAR(100)   NOT NULL,
    "coverage_name"        VARCHAR(255)   NOT NULL,
    "requested_limit"      DECIMAL(15, 2) NOT NULL,
    "offered_limit"        DECIMAL(15, 2) NULL,
    "requested_deductible" DECIMAL(15, 2) NOT NULL,
    "offered_deductible"   DECIMAL(15, 2) NULL,
    "coverage_premium"     DECIMAL(15, 2) NULL,
    "conditions"           VARCHAR(20000) NULL,
    "exclusions"           VARCHAR(20000) NULL,
    "waiting_period_days"  INTEGER        NULL,
    "created_at"           TIMESTAMP(6)   NOT NULL,
    "updated_at"           TIMESTAMP(6)   NOT NULL,
    CONSTRAINT "pk_quote_coverage" PRIMARY KEY ("quote_coverage_id"),
    CONSTRAINT "uk_quote_coverage_code" UNIQUE ("quote_id", "coverage_code"),
    CONSTRAINT "fk_quote_coverage_quote" FOREIGN KEY ("quote_id")
        REFERENCES "quote" ("quote_id")
);

CREATE TABLE IF NOT EXISTS "underwriting_decision" (
    "underwriting_decision_id" VARCHAR(36)    NOT NULL,
    "source_decision_id"       VARCHAR(36)    NULL,
    "quote_id"                 VARCHAR(36)    NOT NULL,
    "underwriter_id"           VARCHAR(36)    NOT NULL,
    "decision"                 VARCHAR(50)    NOT NULL,
    "decision_reason"          VARCHAR(20000) NOT NULL,
    "authority_level"          VARCHAR(100)   NULL,
    "conditions"               VARCHAR(20000) NULL,
    "decided_at"               TIMESTAMP(6)   NOT NULL,
    "created_at"               TIMESTAMP(6)   NOT NULL,
    CONSTRAINT "pk_underwriting_decision" PRIMARY KEY ("underwriting_decision_id"),
    CONSTRAINT "uk_underwriting_source_decision" UNIQUE ("source_decision_id"),
    CONSTRAINT "fk_underwriting_decision_quote" FOREIGN KEY ("quote_id")
        REFERENCES "quote" ("quote_id")
);

CREATE TABLE IF NOT EXISTS "quote_version" (
    "quote_version_id"       VARCHAR(36)    NOT NULL,
    "quote_id"               VARCHAR(36)    NOT NULL,
    "version"                BIGINT         NOT NULL,
    "total_premium"          DECIMAL(15, 2) NULL,
    "coverage_snapshot"      VARCHAR(20000) NOT NULL,
    "offered_by_user_id"     VARCHAR(36)    NOT NULL,
    "offered_at"             TIMESTAMP(6)   NOT NULL,
    "created_at"             TIMESTAMP(6)   NOT NULL,
    CONSTRAINT "pk_quote_version" PRIMARY KEY ("quote_version_id"),
    CONSTRAINT "uk_quote_version" UNIQUE ("quote_id", "version"),
    CONSTRAINT "fk_quote_version_quote" FOREIGN KEY ("quote_id")
        REFERENCES "quote" ("quote_id")
);

CREATE TABLE IF NOT EXISTS "policy" (
    "policy_id"         VARCHAR(36)    NOT NULL,
    "policy_number"     VARCHAR(50)    NOT NULL,
    "quote_id"          VARCHAR(36)    NOT NULL,
    "customer_id"       VARCHAR(36)    NOT NULL,
    "product_code"      VARCHAR(100)   NOT NULL,
    "status"            VARCHAR(50)    NOT NULL,
    "start_date"        DATE           NOT NULL,
    "end_date"          DATE           NOT NULL,
    "total_premium"     DECIMAL(15, 2) NOT NULL,
    "issued_by_user_id" VARCHAR(36)    NOT NULL,
    "bound_at"          TIMESTAMP(6)   NOT NULL,
    "issued_at"         TIMESTAMP(6)   NULL,
    "expired_at"        TIMESTAMP(6)   NULL,
    "created_at"        TIMESTAMP(6)   NOT NULL,
    "updated_at"        TIMESTAMP(6)   NOT NULL,
    CONSTRAINT "pk_policy" PRIMARY KEY ("policy_id"),
    CONSTRAINT "uk_policy_number" UNIQUE ("policy_number"),
    CONSTRAINT "uk_policy_quote_id" UNIQUE ("quote_id"),
    CONSTRAINT "fk_policy_quote" FOREIGN KEY ("quote_id")
        REFERENCES "quote" ("quote_id")
);

CREATE TABLE IF NOT EXISTS "policy_coverage" (
    "policy_coverage_id"  VARCHAR(36)    NOT NULL,
    "policy_id"           VARCHAR(36)    NOT NULL,
    "coverage_code"       VARCHAR(100)   NOT NULL,
    "coverage_name"       VARCHAR(255)   NOT NULL,
    "limit_amount"        DECIMAL(15, 2) NOT NULL,
    "deductible_amount"   DECIMAL(15, 2) NOT NULL,
    "coverage_premium"    DECIMAL(15, 2) NOT NULL,
    "conditions"          VARCHAR(20000) NULL,
    "exclusions"          VARCHAR(20000) NULL,
    "waiting_period_days" INTEGER        NULL,
    "effective_from"      DATE           NOT NULL,
    "effective_to"        DATE           NOT NULL,
    "created_at"          TIMESTAMP(6)   NOT NULL,
    CONSTRAINT "pk_policy_coverage" PRIMARY KEY ("policy_coverage_id"),
    CONSTRAINT "uk_policy_coverage_code" UNIQUE ("policy_id", "coverage_code"),
    CONSTRAINT "fk_policy_coverage_policy" FOREIGN KEY ("policy_id")
        REFERENCES "policy" ("policy_id")
);

CREATE TABLE IF NOT EXISTS "endorsement" (
    "endorsement_id"         VARCHAR(36)    NOT NULL,
    "policy_id"              VARCHAR(36)    NOT NULL,
    "endorsement_number"     VARCHAR(50)    NOT NULL,
    "endorsement_type"       VARCHAR(50)    NOT NULL,
    "description"            VARCHAR(20000) NOT NULL,
    "premium_delta"          DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    "status"                 VARCHAR(50)    NOT NULL,
    "requested_by_user_id"   VARCHAR(36)    NOT NULL,
    "approved_by_user_id"    VARCHAR(36)    NULL,
    "effective_from"         DATE           NOT NULL,
    "effective_to"           DATE           NULL,
    "requested_at"           TIMESTAMP(6)   NOT NULL,
    "approved_at"            TIMESTAMP(6)   NULL,
    "created_at"             TIMESTAMP(6)   NOT NULL,
    "updated_at"             TIMESTAMP(6)   NOT NULL,
    CONSTRAINT "pk_endorsement" PRIMARY KEY ("endorsement_id"),
    CONSTRAINT "uk_endorsement_number" UNIQUE ("endorsement_number"),
    CONSTRAINT "fk_endorsement_policy" FOREIGN KEY ("policy_id")
        REFERENCES "policy" ("policy_id")
);

CREATE TABLE IF NOT EXISTS "endorsement_coverage" (
    "endorsement_coverage_id" VARCHAR(36)    NOT NULL,
    "endorsement_id"          VARCHAR(36)    NOT NULL,
    "coverage_code"           VARCHAR(100)   NOT NULL,
    "coverage_name"           VARCHAR(255)   NOT NULL,
    "limit_amount"            DECIMAL(15, 2) NULL,
    "deductible_amount"       DECIMAL(15, 2) NULL,
    "coverage_premium"        DECIMAL(15, 2) NULL,
    "conditions"              VARCHAR(20000) NULL,
    "exclusions"              VARCHAR(20000) NULL,
    "waiting_period_days"     INTEGER        NULL,
    "operation"               VARCHAR(20)    NOT NULL,
    "created_at"              TIMESTAMP(6)   NOT NULL,
    CONSTRAINT "pk_endorsement_coverage" PRIMARY KEY ("endorsement_coverage_id"),
    CONSTRAINT "fk_endorsement_coverage_endorsement" FOREIGN KEY ("endorsement_id")
        REFERENCES "endorsement" ("endorsement_id")
);

CREATE TABLE IF NOT EXISTS "renewal_transaction" (
    "renewal_id"                  VARCHAR(36)    NOT NULL,
    "policy_id"                   VARCHAR(36)    NOT NULL,
    "renewal_number"              VARCHAR(50)    NOT NULL,
    "status"                      VARCHAR(50)    NOT NULL,
    "proposed_start_date"         DATE           NOT NULL,
    "proposed_end_date"           DATE           NOT NULL,
    "proposed_total_premium"      DECIMAL(15, 2) NULL,
    "proposed_coverage_snapshot"  VARCHAR(20000) NULL,
    "subjectivities"              VARCHAR(20000) NULL,
    "decided_by_user_id"          VARCHAR(36)    NULL,
    "decided_at"                  TIMESTAMP(6)   NULL,
    "decision_reason"             VARCHAR(20000) NULL,
    "bound_by_user_id"            VARCHAR(36)    NULL,
    "bound_at"                    TIMESTAMP(6)   NULL,
    "issued_at"                   TIMESTAMP(6)   NULL,
    "created_at"                  TIMESTAMP(6)   NOT NULL,
    "updated_at"                  TIMESTAMP(6)   NOT NULL,
    CONSTRAINT "pk_renewal_transaction" PRIMARY KEY ("renewal_id"),
    CONSTRAINT "uk_renewal_number" UNIQUE ("renewal_number"),
    CONSTRAINT "fk_renewal_policy" FOREIGN KEY ("policy_id")
        REFERENCES "policy" ("policy_id")
);

CREATE TABLE IF NOT EXISTS "premium_audit" (
    "audit_id"               VARCHAR(36)    NOT NULL,
    "policy_id"              VARCHAR(36)    NOT NULL,
    "audit_number"           VARCHAR(50)    NOT NULL,
    "audit_type"             VARCHAR(50)    NOT NULL,
    "status"                 VARCHAR(50)    NOT NULL,
    "estimated_exposure"     DECIMAL(15, 2) NOT NULL,
    "actual_exposure"        DECIMAL(15, 2) NULL,
    "exposure_basis"         VARCHAR(100)   NOT NULL,
    "premium_delta"          DECIMAL(15, 2) NULL,
    "additional_premium"     DECIMAL(15, 2) NULL,
    "return_premium"         DECIMAL(15, 2) NULL,
    "audited_by_user_id"     VARCHAR(36)    NULL,
    "audited_at"             TIMESTAMP(6)   NULL,
    "created_at"             TIMESTAMP(6)   NOT NULL,
    "updated_at"             TIMESTAMP(6)   NOT NULL,
    CONSTRAINT "pk_premium_audit" PRIMARY KEY ("audit_id"),
    CONSTRAINT "uk_audit_number" UNIQUE ("audit_number"),
    CONSTRAINT "fk_audit_policy" FOREIGN KEY ("policy_id")
        REFERENCES "policy" ("policy_id")
);

CREATE TABLE IF NOT EXISTS "subjectivity" (
    "subjectivity_id"           VARCHAR(36)    NOT NULL,
    "quote_id"                  VARCHAR(36)    NOT NULL,
    "subjectivity_code"         VARCHAR(50)    NOT NULL,
    "description"               VARCHAR(20000) NOT NULL,
    "status"                    VARCHAR(50)    NOT NULL,
    "satisfied_by_user_id"      VARCHAR(36)    NULL,
    "satisfied_at"              TIMESTAMP(6)   NULL,
    "evidence_document_ids"     VARCHAR(20000) NULL,
    "created_at"                TIMESTAMP(6)   NOT NULL,
    "updated_at"                TIMESTAMP(6)   NOT NULL,
    CONSTRAINT "pk_subjectivity" PRIMARY KEY ("subjectivity_id"),
    CONSTRAINT "fk_subjectivity_quote" FOREIGN KEY ("quote_id")
        REFERENCES "quote" ("quote_id")
);