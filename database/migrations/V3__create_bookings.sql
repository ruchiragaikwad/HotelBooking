CREATE TABLE bookings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_reference VARCHAR(36) NOT NULL,
    hotel_id BIGINT NOT NULL,
    customer_name VARCHAR(150) NOT NULL,
    customer_email VARCHAR(254) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    nights INT NOT NULL,
    nightly_price_snapshot DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    cancelled_at TIMESTAMP(6) NULL,
    CONSTRAINT pk_bookings PRIMARY KEY (id),
    CONSTRAINT uk_bookings_reference UNIQUE (booking_reference),
    CONSTRAINT fk_bookings_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id),
    CONSTRAINT chk_bookings_dates CHECK (check_out > check_in),
    CONSTRAINT chk_bookings_nights CHECK (nights > 0),
    CONSTRAINT chk_bookings_nightly_price CHECK (nightly_price_snapshot > 0),
    CONSTRAINT chk_bookings_total CHECK (total_amount > 0),
    CONSTRAINT chk_bookings_status CHECK (status IN ('CONFIRMED', 'CANCELLED'))
);

CREATE INDEX idx_bookings_hotel_status_dates
    ON bookings (hotel_id, status, check_in, check_out);
