package com.hotelbooking.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "hotels", indexes = @Index(name = "idx_hotels_location", columnList = "location"))
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 120)
    private String location;

    @Column(nullable = false, length = 1000)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hotel_amenities", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "amenity", nullable = false, length = 100)
    @OrderBy("amenity ASC")
    private Set<String> amenities = new HashSet<>();

    @Column(name = "nightly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal nightlyPrice;

    @Column(name = "room_capacity", nullable = false)
    private int roomCapacity;

    protected Hotel() {
        // Required by JPA when it loads a hotel from the database.
    }

    public Hotel(
            String name,
            String location,
            String description,
            Set<String> amenities,
            BigDecimal nightlyPrice,
            int roomCapacity
    ) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.amenities = new HashSet<>(amenities);
        this.nightlyPrice = nightlyPrice;
        this.roomCapacity = roomCapacity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getAmenities() {
        return Set.copyOf(amenities);
    }

    public BigDecimal getNightlyPrice() {
        return nightlyPrice;
    }

    public int getRoomCapacity() {
        return roomCapacity;
    }
}
