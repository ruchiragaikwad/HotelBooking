package com.hotelbooking.dto;

import com.hotelbooking.entities.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BookingResponse(
        String bookingReference,
        Long hotelId,
        String hotelName,
        String customerName,
        String customerEmail,
        LocalDate checkIn,
        LocalDate checkOut,
        int nights,
        BigDecimal nightlyPrice,
        BigDecimal totalAmount,
        BookingStatus status,
        Instant createdAt,
        Instant cancelledAt
) {
}
