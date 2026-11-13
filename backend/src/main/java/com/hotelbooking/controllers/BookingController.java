package com.hotelbooking.controllers;

import com.hotelbooking.dto.BookingLookupRequest;
import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.dto.CancelBookingRequest;
import com.hotelbooking.dto.CreateBookingRequest;
import com.hotelbooking.services.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @PostMapping("/lookup")
    public BookingResponse findBooking(@Valid @RequestBody BookingLookupRequest request) {
        return bookingService.findBooking(request);
    }

    @PostMapping("/{bookingReference}/cancel")
    public BookingResponse cancelBooking(
            @PathVariable String bookingReference,
            @Valid @RequestBody CancelBookingRequest request
    ) {
        return bookingService.cancelBooking(bookingReference, request);
    }
}
