CREATE TABLE hotels (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(120) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    nightly_price DECIMAL(10, 2) NOT NULL,
    room_capacity INT NOT NULL,
    CONSTRAINT pk_hotels PRIMARY KEY (id),
    CONSTRAINT chk_hotels_nightly_price CHECK (nightly_price > 0),
    CONSTRAINT chk_hotels_room_capacity CHECK (room_capacity > 0)
);

CREATE INDEX idx_hotels_location ON hotels (location);

CREATE TABLE hotel_amenities (
    hotel_id BIGINT NOT NULL,
    amenity VARCHAR(100) NOT NULL,
    CONSTRAINT pk_hotel_amenities PRIMARY KEY (hotel_id, amenity),
    CONSTRAINT fk_hotel_amenities_hotel FOREIGN KEY (hotel_id)
        REFERENCES hotels (id) ON DELETE CASCADE
);
