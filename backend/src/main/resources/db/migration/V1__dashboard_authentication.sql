CREATE TABLE staff_users (
    id BINARY(16) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    assigned_hotel_id BIGINT NULL,
    must_change_password BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP(6) NULL,
    last_login_at TIMESTAMP(6) NULL,
    created_by BINARY(16) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_staff_users_email UNIQUE (email),
    INDEX idx_staff_users_role_status (role, status)
);

CREATE TABLE security_audit (
    id BIGINT NOT NULL AUTO_INCREMENT,
    actor_user_id BINARY(16) NULL,
    target_user_id BINARY(16) NULL,
    event_type VARCHAR(40) NOT NULL,
    occurred_at TIMESTAMP(6) NOT NULL,
    ip_address VARCHAR(64) NULL,
    result VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_security_audit_occurred (occurred_at),
    INDEX idx_security_audit_target (target_user_id)
);
