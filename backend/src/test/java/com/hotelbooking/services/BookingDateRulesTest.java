package com.hotelbooking.services;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingDateRulesTest {

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-01-01T10:00:00Z"),
            ZoneId.of("Asia/Kolkata")
    );

    @Test
    void acceptsCheckInTodayAndCountsOneNight() {
        LocalDate today = LocalDate.now(clock);

        BookingDateRules.validate(today, today.plusDays(1), clock);

        assertThat(BookingDateRules.nights(today, today.plusDays(1))).isEqualTo(1);
    }

    @Test
    void countsMultipleNights() {
        LocalDate checkIn = LocalDate.now(clock).plusDays(3);

        assertThat(BookingDateRules.nights(checkIn, checkIn.plusDays(4))).isEqualTo(4);
    }

    @Test
    void rejectsCheckInInThePast() {
        LocalDate yesterday = LocalDate.now(clock).minusDays(1);

        assertThatThrownBy(() -> BookingDateRules.validate(yesterday, yesterday.plusDays(1), clock))
                .isInstanceOf(BookingRuleViolationException.class)
                .hasMessage("Check-in date cannot be in the past.");
    }

    @Test
    void rejectsSameDayOrEarlierCheckout() {
        LocalDate today = LocalDate.now(clock);

        assertThatThrownBy(() -> BookingDateRules.validate(today, today, clock))
                .isInstanceOf(BookingRuleViolationException.class)
                .hasMessage("Check-out must be after check-in.");
        assertThatThrownBy(() -> BookingDateRules.validate(today, today.minusDays(1), clock))
                .isInstanceOf(BookingRuleViolationException.class)
                .hasMessage("Check-out must be after check-in.");
    }

    @Test
    void overlapUsesHalfOpenDateIntervals() {
        LocalDate checkIn = LocalDate.of(2026, 3, 10);
        LocalDate checkOut = LocalDate.of(2026, 3, 15);

        assertThat(BookingDateRules.overlaps(checkIn, checkOut, checkIn, checkOut)).isTrue();
        assertThat(BookingDateRules.overlaps(checkIn, checkOut, checkIn.plusDays(2), checkOut.plusDays(2)))
                .isTrue();
        assertThat(BookingDateRules.overlaps(checkIn, checkOut, checkIn.plusDays(1), checkOut.minusDays(1)))
                .isTrue();
        assertThat(BookingDateRules.overlaps(checkIn, checkOut, checkOut, checkOut.plusDays(1)))
                .isFalse();
        assertThat(BookingDateRules.overlaps(checkIn, checkOut, checkIn.minusDays(1), checkIn))
                .isFalse();
    }
}
