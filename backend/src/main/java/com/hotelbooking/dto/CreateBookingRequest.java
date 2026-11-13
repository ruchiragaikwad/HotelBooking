package com.hotelbooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateBookingRequest(
        @NotNull @Positive Long hotelId,
        @NotBlank @Size(max = 150) String customerName,
        @NotBlank @Email @Size(max = 254) String customerEmail,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut
) {
}
