package com.example.tourismfx.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Hotel.java - Hotel Entity
 * ==========================
 * Represents a hotel or accommodation property in the tourism system.
 * Hotels contain rooms that tourists can browse and book.
 * 
 * Each hotel has a star rating (1-5), location details, and a collection
 * of available rooms with different types and pricing.
 * 
 * Table: hotels
 * Relationships:
 *   - One-to-Many with Room (a hotel has multiple rooms)
 */
@Entity
@Table(name = "hotels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {

    /** Primary key: auto-incremented unique identifier */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Hotel name: displayed prominently in listings and search results */
    @NotBlank(message = "Hotel name is required")
    @Column(nullable = false, length = 100)
    private String name;

    /** Description: detailed information about the hotel, amenities, etc. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Location/city: where the hotel is situated (e.g., "Nairobi", "Mombasa") */
    @NotBlank(message = "Location is required")
    @Column(nullable = false, length = 100)
    private String location;

    /** Full address: street address for directions and receipts */
    @Column(length = 255)
    private String address;

    /**
     * Star rating: hotel quality rating from 1 (budget) to 5 (luxury).
     * Used for filtering and sorting in the hotel listing page.
     */
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    @Column(nullable = false)
    private int starRating;

    /** Image URL: path to the hotel's main photograph for the listing card */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** Contact phone: hotel reception number for guest inquiries */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /** Contact email: hotel email address for reservations */
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    /** Active status: allows admin to hide hotels without deleting them */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Rooms: all rooms belonging to this hotel.
     * mappedBy indicates that Room owns the foreign key relationship.
     * CascadeType.ALL ensures room operations cascade from the hotel.
     * orphanRemoval deletes rooms removed from this collection.
     */
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();
}
