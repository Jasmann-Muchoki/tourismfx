package com.example.tourismfx.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Room.java - Hotel Room Entity
 * ===============================
 * Represents an individual room type within a hotel.
 * Each room has a type (e.g., Standard, Deluxe, Suite), a price per night,
 * and availability tracking.
 * 
 * Rooms are the bookable units — tourists select a room type when making
 * a reservation. The price is stored as BigDecimal for exact monetary
 * calculations (avoids floating-point precision issues).
 * 
 * Table: rooms
 * Relationships:
 *   - Many-to-One with Hotel (each room belongs to one hotel)
 *   - One-to-Many with HotelBooking (a room can be booked multiple times)
 */
@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    /** Primary key: auto-incremented unique identifier */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hotel: the hotel this room belongs to.
     * FetchType.LAZY delays loading the hotel until it's accessed,
     * improving performance when listing many rooms.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    /**
     * Room type: category of the room (e.g., "Standard", "Deluxe", "Suite").
     * Displayed in the booking form for tourist selection.
     */
    @NotBlank(message = "Room type is required")
    @Column(name = "room_type", nullable = false, length = 50)
    private String roomType;

    /** Description: details about the room's amenities and features */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Price per night: in the hotel's base currency (e.g., KES or USD).
     * BigDecimal ensures exact monetary arithmetic without rounding errors.
     * precision=10, scale=2 supports values up to 99,999,999.99
     */
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    /** Maximum occupancy: how many guests the room can accommodate */
    @Min(value = 1, message = "Capacity must be at least 1")
    @Column(nullable = false)
    private int capacity = 2;

    /** Total count: total number of rooms of this type in the hotel */
    @Min(value = 0, message = "Total rooms cannot be negative")
    @Column(name = "total_rooms", nullable = false)
    private int totalRooms = 1;

    /** Available count: how many rooms of this type are currently available */
    @Min(value = 0, message = "Available rooms cannot be negative")
    @Column(name = "available_rooms", nullable = false)
    private int availableRooms = 1;

    /** Image URL: photo of the room for the listing page */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** Active status: allows admin to disable room types without deleting */
    @Column(nullable = false)
    private boolean active = true;
}
