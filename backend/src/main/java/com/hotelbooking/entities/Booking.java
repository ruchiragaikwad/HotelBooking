package com.hotelbooking.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "bookings",
        uniqueConstraints = @UniqueConstraint(name = "uk_bookings_reference", columnNames = "booking_reference"),
        indexes = @Index(
                name = "idx_bookings_hotel_status_dates",
                columnList = "hotel_id, status, check_in, check_out"
        )
)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, length = 36)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 254)
    private String customerEmail;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private int nights;

    @Column(name = "nightly_price_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal nightlyPriceSnapshot;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    protected Booking() {
        // Required by JPA when it loads a booking from the database.
    }

    public Booking(
            String bookingReference,
            Hotel hotel,
            String customerName,
            String customerEmail,
            LocalDate checkIn,
            LocalDate checkOut,
            int nights,
            BigDecimal nightlyPriceSnapshot,
            BigDecimal totalAmount,
            Instant createdAt
    ) {
        this.bookingReference = bookingReference;
        this.hotel = hotel;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.nights = nights;
        this.nightlyPriceSnapshot = nightlyPriceSnapshot;
        this.totalAmount = totalAmount;
        this.status = BookingStatus.CONFIRMED;
        this.createdAt = createdAt;
    }

    public void cancel(Instant cancelledAt) {
        if (status == BookingStatus.CONFIRMED) {
            status = BookingStatus.CANCELLED;
            this.cancelledAt = cancelledAt;
        }
    }

    public Long getId() {
        return id;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public int getNights() {
        return nights;
    }

    public BigDecimal getNightlyPriceSnapshot() {
        return nightlyPriceSnapshot;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }
}
