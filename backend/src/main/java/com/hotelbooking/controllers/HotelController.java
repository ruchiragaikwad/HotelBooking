package com.hotelbooking.controllers;

import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.dto.AvailabilityResponse;
import com.hotelbooking.services.BookingService;
import com.hotelbooking.services.HotelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final BookingService bookingService;

    public HotelController(HotelService hotelService, BookingService bookingService) {
        this.hotelService = hotelService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<HotelResponse> getHotels(@RequestParam(required = false) String location) {
        return hotelService.getHotels(location);
    }

    @GetMapping("/{hotelId}")
    public HotelResponse getHotel(@PathVariable Long hotelId) {
        return hotelService.getHotel(hotelId);
    }

    @GetMapping("/{hotelId}/availability")
    public AvailabilityResponse getAvailability(
            @PathVariable Long hotelId,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut
    ) {
        return bookingService.getAvailability(hotelId, checkIn, checkOut);
    }
}
