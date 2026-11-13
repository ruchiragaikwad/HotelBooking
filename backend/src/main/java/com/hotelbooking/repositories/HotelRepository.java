package com.hotelbooking.repositories;

import com.hotelbooking.entities.Hotel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findAllByOrderByNameAsc();

    List<Hotel> findByLocationContainingIgnoreCaseOrderByNameAsc(String location);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select hotel from Hotel hotel where hotel.id = :hotelId")
    Optional<Hotel> findByIdForUpdate(@Param("hotelId") Long hotelId);
}
