package com.hotelbooking.services;

import com.hotelbooking.dto.AvailabilityResponse;
import com.hotelbooking.dto.BookingLookupRequest;
import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.dto.CancelBookingRequest;
import com.hotelbooking.dto.CreateBookingRequest;
import com.hotelbooking.entities.Booking;
import com.hotelbooking.entities.BookingStatus;
import com.hotelbooking.entities.Hotel;
import com.hotelbooking.exceptions.BookingNotFoundException;
import com.hotelbooking.exceptions.HotelNotFoundException;
import com.hotelbooking.repositories.BookingRepository;
import com.hotelbooking.repositories.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final Clock bookingClock;

    public BookingService(
            BookingRepository bookingRepository,
            HotelRepository hotelRepository,
            Clock bookingClock
    ) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.bookingClock = bookingClock;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        BookingDateRules.validate(checkIn, checkOut, bookingClock);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));
        int nights = BookingDateRules.nights(checkIn, checkOut);
        long overlappingBookings = bookingRepository.countOverlappingBookings(
                hotelId,
                BookingStatus.CONFIRMED,
                checkIn,
                checkOut
        );
        int bookedRooms = Math.toIntExact(overlappingBookings);
        int availableRooms = Math.max(0, hotel.getRoomCapacity() - bookedRooms);

        return new AvailabilityResponse(
                hotelId,
                checkIn,
                checkOut,
                nights,
                hotel.getRoomCapacity(),
                bookedRooms,
                availableRooms,
                availableRooms > 0
        );
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        BookingDateRules.validate(request.checkIn(), request.checkOut(), bookingClock);

        // Lock the hotel row before checking capacity so competing requests serialize here.
        Hotel hotel = hotelRepository.findByIdForUpdate(request.hotelId())
                .orElseThrow(() -> new HotelNotFoundException(request.hotelId()));

        long overlappingBookings = bookingRepository.countOverlappingBookings(
                hotel.getId(),
                BookingStatus.CONFIRMED,
                request.checkIn(),
                request.checkOut()
        );
        if (overlappingBookings >= hotel.getRoomCapacity()) {
            throw new RoomUnavailableException();
        }

        int nights = BookingDateRules.nights(request.checkIn(), request.checkOut());
        BigDecimal nightlyPrice = hotel.getNightlyPrice();
        BigDecimal totalAmount = nightlyPrice.multiply(BigDecimal.valueOf(nights));
        Booking booking = new Booking(
                UUID.randomUUID().toString().toUpperCase(Locale.ROOT),
                hotel,
                request.customerName().trim(),
                normalizeEmail(request.customerEmail()),
                request.checkIn(),
                request.checkOut(),
                nights,
                nightlyPrice,
                totalAmount,
                bookingClock.instant()
        );

        return toResponse(bookingRepository.saveAndFlush(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse findBooking(BookingLookupRequest request) {
        Booking booking = bookingRepository.findByBookingReferenceAndCustomerEmail(
                        normalizeReference(request.bookingReference()),
                        normalizeEmail(request.customerEmail())
                )
                .orElseThrow(BookingNotFoundException::new);
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(String bookingReference, CancelBookingRequest request) {
        Booking booking = bookingRepository.findByBookingReferenceAndCustomerEmail(
                        normalizeReference(bookingReference),
                        normalizeEmail(request.customerEmail())
                )
                .orElseThrow(BookingNotFoundException::new);

        // Repeated cancellation is safe: an already-cancelled booking remains cancelled.
        booking.cancel(bookingClock.instant());
        return toResponse(booking);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getBookingReference(),
                booking.getHotel().getId(),
                booking.getHotel().getName(),
                booking.getCustomerName(),
                booking.getCustomerEmail(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getNights(),
                booking.getNightlyPriceSnapshot(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getCancelledAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeReference(String reference) {
        return reference.trim().toUpperCase(Locale.ROOT);
    }
}
