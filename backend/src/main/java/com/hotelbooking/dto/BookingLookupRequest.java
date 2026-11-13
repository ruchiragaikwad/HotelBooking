package com.hotelbooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookingLookupRequest(
        @NotBlank @Size(max = 36) String bookingReference,
        @NotBlank @Email @Size(max = 254) String customerEmail
) {
}
