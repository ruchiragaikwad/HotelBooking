package com.hotelbooking;

import com.hotelbooking.entities.Hotel;
import com.hotelbooking.repositories.HotelRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HotelBookingApplicationTests {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void migrationCreatesHotelTablesAndRepositoryCanPersistHotel() {
        Hotel hotel = new Hotel(
                "Test Hotel",
                "Pune",
                "A hotel used by the persistence test.",
                Set.of("Wi-Fi", "Breakfast"),
                new BigDecimal("125.50"),
                12
        );

        Hotel savedHotel = hotelRepository.saveAndFlush(hotel);
        entityManager.clear();

        Hotel loadedHotel = hotelRepository.findById(savedHotel.getId()).orElseThrow();

        assertThat(loadedHotel.getName()).isEqualTo("Test Hotel");
        assertThat(loadedHotel.getLocation()).isEqualTo("Pune");
        assertThat(loadedHotel.getAmenities()).containsExactlyInAnyOrder("Wi-Fi", "Breakfast");
        assertThat(loadedHotel.getNightlyPrice()).isEqualByComparingTo("125.50");
        assertThat(loadedHotel.getRoomCapacity()).isEqualTo(12);
    }

    @Test
    void repositoryListsFiveSeededHotelsAndSearchesLocationWithoutCaseSensitivity() {
        assertThat(hotelRepository.findAllByOrderByNameAsc()).hasSize(5);
        assertThat(hotelRepository.findByLocationContainingIgnoreCaseOrderByNameAsc("gOA"))
                .extracting(Hotel::getName)
                .containsExactly("Aurora Bay Resort");
    }

    @Test
    void hotelListApiReturnsHotelDetails() throws Exception {
        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(5)))
                .andExpect(jsonPath("$[0].name", is("Aurora Bay Resort")))
                .andExpect(jsonPath("$[0].location", is("Goa")))
                .andExpect(jsonPath("$[0].amenities[0]").exists())
                .andExpect(jsonPath("$[0].nightlyPrice").value(6500.0));
    }

    @Test
    void locationSearchApiReturnsMatchingHotels() throws Exception {
        mockMvc.perform(get("/api/hotels").param("location", "mUm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("Citylight Grand")))
                .andExpect(jsonPath("$[0].location", is("Mumbai")));
    }

    @Test
    void hotelDetailsApiReturnsOneHotel() throws Exception {
        mockMvc.perform(get("/api/hotels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Aurora Bay Resort")))
                .andExpect(jsonPath("$.roomCapacity", is(24)));
    }

    @Test
    void missingHotelReturnsStandardNotFoundError() throws Exception {
        mockMvc.perform(get("/api/hotels/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.path", is("/api/hotels/99999")))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void nonNumericHotelIdReturnsStandardBadRequestError() throws Exception {
        mockMvc.perform(get("/api/hotels/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.path", is("/api/hotels/not-a-number")));
    }
}
