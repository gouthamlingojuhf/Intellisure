CREATE TABLE IF NOT EXISTS user_account (
                                            user_id BINARY(16) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    account_status VARCHAR(50) NOT NULL,
    display_name VARCHAR(255),
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
    );

CREATE TABLE IF NOT EXISTS business_customer (
    customer_id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL UNIQUE,
    business_name VARCHAR(255) NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    business_type VARCHAR(100),
    phone VARCHAR(50),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(50),
    country VARCHAR(50),
    postal_code VARCHAR(20),
    version BIGINT NOT NULL DEFAULT 0,
    last_material_change_at DATETIME NULL,
    material_change_pending BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
);

CREATE TABLE IF NOT EXISTS user_role_audit (
    audit_id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    old_roles JSON NULL,
    new_roles JSON NOT NULL,
    changed_by BINARY(16) NOT NULL,
    changed_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_account(user_id),
    FOREIGN KEY (changed_by) REFERENCES user_account(user_id)
);

-- Indexes are created programmatically or via migration
-- CREATE INDEX idx_user_role_audit_user_id ON user_role_audit(user_id);
-- CREATE INDEX idx_user_role_audit_changed_at ON user_role_audit(changed_at);
