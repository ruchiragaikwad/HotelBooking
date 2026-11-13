package com.hotelbooking.services;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class BookingDateRules {

    private BookingDateRules() {
    }

    public static void validate(LocalDate checkIn, LocalDate checkOut, Clock clock) {
        if (checkIn == null || checkOut == null) {
            throw new BookingRuleViolationException("Check-in and check-out dates are required.");
        }
        if (checkIn.isBefore(LocalDate.now(clock))) {
            throw new BookingRuleViolationException("Check-in date cannot be in the past.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BookingRuleViolationException("Check-out must be after check-in.");
        }
    }

    public static int nights(LocalDate checkIn, LocalDate checkOut) {
        return Math.toIntExact(ChronoUnit.DAYS.between(checkIn, checkOut));
    }

    public static boolean overlaps(
            LocalDate existingCheckIn,
            LocalDate existingCheckOut,
            LocalDate requestedCheckIn,
            LocalDate requestedCheckOut
    ) {
        return existingCheckIn.isBefore(requestedCheckOut)
                && existingCheckOut.isAfter(requestedCheckIn);
    }
}
