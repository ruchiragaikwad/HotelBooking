package com.hotelbooking;

import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.dto.CreateBookingRequest;
import com.hotelbooking.entities.Hotel;
import com.hotelbooking.entities.BookingStatus;
import com.hotelbooking.repositories.BookingRepository;
import com.hotelbooking.repositories.HotelRepository;
import com.hotelbooking.services.BookingService;
import com.hotelbooking.services.RoomUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:booking_concurrency_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
})
@ActiveProfiles("test")
class BookingConcurrencyIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private Clock bookingClock;

    @Test
    void concurrentRequestsForTheLastRoomAllowExactlyOneBooking() throws Exception {
        Hotel hotel = hotelRepository.saveAndFlush(new Hotel(
                "Single Room Test Hotel",
                "Concurrency City",
                "Used only for the concurrent booking test.",
                Set.of("Wi-Fi"),
                new BigDecimal("200.00"),
                1
        ));
        LocalDate checkIn = LocalDate.now(bookingClock).plusDays(20);
        CreateBookingRequest firstRequest = request(hotel.getId(), "first@example.com", checkIn);
        CreateBookingRequest secondRequest = request(hotel.getId(), "second@example.com", checkIn);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Attempt> first = executor.submit(() -> attempt(firstRequest, ready, start));
            Future<Attempt> second = executor.submit(() -> attempt(secondRequest, ready, start));

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            Attempt firstResult = first.get(10, TimeUnit.SECONDS);
            Attempt secondResult = second.get(10, TimeUnit.SECONDS);

            assertThat((int) (firstResult.booked() ? 1 : 0) + (secondResult.booked() ? 1 : 0)).isEqualTo(1);
            assertThat((int) (firstResult.unavailable() ? 1 : 0) + (secondResult.unavailable() ? 1 : 0))
                    .isEqualTo(1);
            assertThat(bookingRepository.countOverlappingBookings(
                    hotel.getId(),
                    BookingStatus.CONFIRMED,
                    checkIn,
                    checkIn.plusDays(1)
            )).isEqualTo(1);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private Attempt attempt(
            CreateBookingRequest request,
            CountDownLatch ready,
            CountDownLatch start
    ) throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            BookingResponse response = bookingService.createBooking(request);
            return new Attempt(true, false, response);
        } catch (RoomUnavailableException exception) {
            return new Attempt(false, true, null);
        }
    }

    private CreateBookingRequest request(Long hotelId, String email, LocalDate checkIn) {
        return new CreateBookingRequest(
                hotelId,
                "Concurrent Guest",
                email,
                checkIn,
                checkIn.plusDays(1)
        );
    }

    private record Attempt(boolean booked, boolean unavailable, BookingResponse response) {
    }
}
