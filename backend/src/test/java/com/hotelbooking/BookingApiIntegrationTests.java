package com.hotelbooking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.entities.Hotel;
import com.hotelbooking.repositories.HotelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookingApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private Clock bookingClock;

    @Test
    void availabilityStartsAtFullCapacityAndIncludesNightCount() throws Exception {
        Hotel hotel = createHotel(1);
        LocalDate checkIn = LocalDate.now(bookingClock).plusDays(4);

        mockMvc.perform(get("/api/hotels/{hotelId}/availability", hotel.getId())
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkIn.plusDays(2).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nights", is(2)))
                .andExpect(jsonPath("$.roomCapacity", is(1)))
                .andExpect(jsonPath("$.bookedRooms", is(0)))
                .andExpect(jsonPath("$.availableRooms", is(1)))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void createsOneNightAndMultiNightBookingsWithBackendCalculatedTotals() throws Exception {
        Hotel hotel = createHotel(3);
        LocalDate checkIn = LocalDate.now(bookingClock).plusDays(5);

        mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(createRequest(hotel.getId(), "Guest One", "Guest@Example.com", checkIn,
                                checkIn.plusDays(2), "0.01")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingReference").isString())
                .andExpect(jsonPath("$.bookingReference").value(org.hamcrest.Matchers.hasLength(36)))
                .andExpect(jsonPath("$.nights", is(2)))
                .andExpect(jsonPath("$.nightlyPrice").value(100.25))
                .andExpect(jsonPath("$.totalAmount").value(200.5))
                .andExpect(jsonPath("$.customerEmail", is("guest@example.com")))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    void separateBookingsReceiveDifferentReferences() throws Exception {
        Hotel hotel = createHotel(2);
        LocalDate firstCheckIn = LocalDate.now(bookingClock).plusDays(8);
        String firstResponse = mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(createRequest(hotel.getId(), "Guest One", "one@example.com", firstCheckIn,
                                firstCheckIn.plusDays(1), null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String secondResponse = mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(createRequest(hotel.getId(), "Guest Two", "two@example.com",
                                firstCheckIn.plusDays(1), firstCheckIn.plusDays(2), null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String firstReference = objectMapper.readTree(firstResponse).get("bookingReference").asText();
        String secondReference = objectMapper.readTree(secondResponse).get("bookingReference").asText();
        assertThat(firstReference).isNotEqualTo(secondReference);
    }

    @Test
    void adjacentStayDoesNotConsumeCapacityButOverlappingStayDoes() throws Exception {
        Hotel hotel = createHotel(1);
        LocalDate checkIn = LocalDate.now(bookingClock).plusDays(6);
        LocalDate checkOut = checkIn.plusDays(2);

        mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(createRequest(hotel.getId(), "Guest One", "one@example.com", checkIn,
                                checkOut, null)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/hotels/{hotelId}/availability", hotel.getId())
                        .param("checkIn", checkOut.toString())
                        .param("checkOut", checkOut.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableRooms", is(1)))
                .andExpect(jsonPath("$.available", is(true)));

        mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(createRequest(hotel.getId(), "Guest Two", "two@example.com",
                                checkIn.plusDays(1), checkOut.plusDays(1), null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("No rooms are available for the selected dates.")));

        mockMvc.perform(get("/api/hotels/{hotelId}/availability", hotel.getId())
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkOut.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookedRooms", is(1)))
                .andExpect(jsonPath("$.availableRooms", is(0)))
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    void lookupRequiresMatchingEmailAndCancellationIsIdempotentAndReleasesRoom() throws Exception {
        Hotel hotel = createHotel(1);
        LocalDate checkIn = LocalDate.now(bookingClock).plusDays(7);
        String createBody = createRequest(hotel.getId(), "Guest One", "guest@example.com", checkIn,
                checkIn.plusDays(1), null);
        JsonNode booking = objectMapper.readTree(mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        String reference = booking.get("bookingReference").asText();

        mockMvc.perform(post("/api/bookings/lookup")
                        .contentType("application/json")
                        .content("""
                                {"bookingReference":"%s","customerEmail":"GUEST@EXAMPLE.COM"}
                                """.formatted(reference)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingReference", is(reference)));

        mockMvc.perform(post("/api/bookings/lookup")
                        .contentType("application/json")
                        .content("""
                                {"bookingReference":"%s","customerEmail":"wrong@example.com"}
                                """.formatted(reference)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

        String cancelBody = """
                {"customerEmail":"GUEST@EXAMPLE.COM"}
                """;
        mockMvc.perform(post("/api/bookings/{reference}/cancel", reference)
                        .contentType("application/json")
                        .content(cancelBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        mockMvc.perform(post("/api/bookings/{reference}/cancel", reference)
                        .contentType("application/json")
                        .content(cancelBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        mockMvc.perform(get("/api/hotels/{hotelId}/availability", hotel.getId())
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkIn.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableRooms", is(1)));
    }

    @Test
    void rejectsPastCheckInSameDayCheckoutAndInvalidCustomerInput() throws Exception {
        Hotel hotel = createHotel(1);
        LocalDate today = LocalDate.now(bookingClock);

        mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(createRequest(hotel.getId(), "Guest", "guest@example.com", today.minusDays(1),
                                today, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Check-in date cannot be in the past.")));

        mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(createRequest(hotel.getId(), "Guest", "guest@example.com", today,
                                today, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Check-out must be after check-in.")));

        mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(createRequest(hotel.getId(), "", "not-an-email", today.plusDays(1),
                                today.plusDays(2), null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message").exists());
    }

    private Hotel createHotel(int capacity) {
        Hotel hotel = new Hotel(
                "Booking Test Hotel",
                "Test City",
                "A hotel for booking API integration tests.",
                Set.of("Wi-Fi"),
                new BigDecimal("100.25"),
                capacity
        );
        return hotelRepository.saveAndFlush(hotel);
    }

    private String createRequest(
            Long hotelId,
            String name,
            String email,
            LocalDate checkIn,
            LocalDate checkOut,
            String untrustedTotal
    ) {
        String totalProperty = untrustedTotal == null ? "" : ",\"totalAmount\":" + untrustedTotal;
        return """
                {"hotelId":%d,"customerName":"%s","customerEmail":"%s","checkIn":"%s","checkOut":"%s"%s}
                """.formatted(hotelId, name, email, checkIn, checkOut, totalProperty);
    }
}
