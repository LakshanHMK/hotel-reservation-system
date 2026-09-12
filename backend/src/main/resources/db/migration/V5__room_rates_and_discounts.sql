CREATE TABLE room_rates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    rate_plan_name VARCHAR(150) NOT NULL,
    rate_plan_code VARCHAR(50) NOT NULL,
    base_nightly_rate DOUBLE NOT NULL,
    weekend_nightly_rate DOUBLE NULL,
    meal_plan VARCHAR(50) NOT NULL DEFAULT 'ROOM_ONLY',
    cancellation_policy VARCHAR(50) NOT NULL DEFAULT 'FLEXIBLE_24H',
    deposit_required BOOLEAN NOT NULL DEFAULT FALSE,
    deposit_percentage DOUBLE DEFAULT 0.0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_room_rates_hotel (hotel_id),
    INDEX idx_room_rates_room (room_id),
    INDEX idx_room_rates_status (status)
);

CREATE TABLE discounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    hotel_id BIGINT NULL,
    code VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NULL,
    discount_type VARCHAR(30) NOT NULL DEFAULT 'PERCENTAGE',
    discount_value DOUBLE NOT NULL,
    minimum_nights INT DEFAULT 1,
    valid_from DATE NULL,
    valid_to DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_discounts_code UNIQUE (code),
    INDEX idx_discounts_hotel (hotel_id),
    INDEX idx_discounts_status (status)
);
