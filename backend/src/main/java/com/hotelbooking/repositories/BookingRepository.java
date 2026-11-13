package com.hotelbooking.repositories;

import com.hotelbooking.entities.Booking;
import com.hotelbooking.entities.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select count(booking)
            from Booking booking
            where booking.hotel.id = :hotelId
              and booking.status = :status
              and booking.checkIn < :checkOut
              and booking.checkOut > :checkIn
            """)
    long countOverlappingBookings(
            @Param("hotelId") Long hotelId,
            @Param("status") BookingStatus status,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    Optional<Booking> findByBookingReferenceAndCustomerEmail(
            String bookingReference,
            String customerEmail
    );
}
