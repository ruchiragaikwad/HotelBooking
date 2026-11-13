package com.hotelbooking.services;

public class BookingRuleViolationException extends RuntimeException {

    public BookingRuleViolationException(String message) {
        super(message);
    }
}
