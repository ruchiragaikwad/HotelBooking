package com.hotelbooking.services;

import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.entities.Hotel;
import com.hotelbooking.exceptions.HotelNotFoundException;
import com.hotelbooking.repositories.HotelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public List<HotelResponse> getHotels(String location) {
        List<Hotel> hotels;
        if (location == null || location.isBlank()) {
            hotels = hotelRepository.findAllByOrderByNameAsc();
        } else {
            hotels = hotelRepository.findByLocationContainingIgnoreCaseOrderByNameAsc(location.trim());
        }
        return hotels.stream().map(this::toResponse).toList();
    }

    public HotelResponse getHotel(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));
        return toResponse(hotel);
    }

    private HotelResponse toResponse(Hotel hotel) {
        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getLocation(),
                hotel.getDescription(),
                hotel.getAmenities().stream().sorted().toList(),
                hotel.getNightlyPrice(),
                hotel.getRoomCapacity()
        );
    }
}
