package com.hotelbooking.dto;

import java.math.BigDecimal;
import java.util.List;

public record HotelResponse(
        Long id,
        String name,
        String location,
        String description,
        List<String> amenities,
        BigDecimal nightlyPrice,
        int roomCapacity
) {
}
