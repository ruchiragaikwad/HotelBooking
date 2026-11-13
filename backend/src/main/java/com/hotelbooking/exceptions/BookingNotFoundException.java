package com.hotelbooking.exceptions;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException() {
        super("Booking reference or customer email is incorrect.");
    }
}
