package com.hotelbooking.exceptions;

public class HotelNotFoundException extends RuntimeException {

    public HotelNotFoundException(Long hotelId) {
        super("Hotel with ID " + hotelId + " was not found.");
    }
}
