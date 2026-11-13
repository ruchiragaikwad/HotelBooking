package com.hotelbooking.dto;

import java.time.LocalDate;

public record AvailabilityResponse(
        Long hotelId,
        LocalDate checkIn,
        LocalDate checkOut,
        int nights,
        int roomCapacity,
        int bookedRooms,
        int availableRooms,
        boolean available
) {
}
