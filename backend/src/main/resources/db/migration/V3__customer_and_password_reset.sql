-- SE2030 LankaStay - Customer Authentication & Password Reset Schema

CREATE TABLE customer_users (
    id BINARY(16) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    phone VARCHAR(30) NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP(6) NULL,
    last_login_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_customer_users_email UNIQUE (email),
    INDEX idx_customer_users_role_status (role, status)
);

CREATE TABLE password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    staff_user_id BINARY(16) NULL,
    customer_user_id BINARY(16) NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    used_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_reset_token_hash (token_hash),
    INDEX idx_reset_expires (expires_at),
    INDEX idx_reset_staff_user (staff_user_id),
    INDEX idx_reset_customer_user (customer_user_id)
);
