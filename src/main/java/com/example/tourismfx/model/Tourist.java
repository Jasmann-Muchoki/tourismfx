package com.example.tourismfx.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Tourist.java - Tourist Profile Entity
 * =======================================
 * Extends the base User with tourism-specific information.
 * Each Tourist is linked to a User account for authentication.
 * 
 * This entity stores additional tourist details like nationality,
 * passport number, and preferred language — information needed
 * for hotel bookings and travel documentation.
 * 
 * Table: tourists
 * Relationships:
 *   - Many-to-One with User (each tourist has one user account)
 *   - One-to-Many with HotelBooking (a tourist can have multiple bookings)
 */
@Entity
@Table(name = "tourists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tourist {

    /** Primary key: auto-incremented unique identifier */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Link to the User account: provides authentication credentials.
     * FetchType.EAGER ensures user data is loaded with the tourist profile.
     * CascadeType.ALL propagates persist/merge/remove operations to User.
     */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    /** Nationality: tourist's country of origin (e.g., "Kenya", "USA") */
    @NotBlank(message = "Nationality is required")
    @Column(nullable = false, length = 50)
    private String nationality;

    /** Passport or ID number: required for hotel check-in and travel */
    @Column(name = "passport_number", length = 30)
    private String passportNumber;

    /** Preferred language for communications (default: English) */
    @Column(name = "preferred_language", length = 20)
    private String preferredLanguage = "English";

    /** Address: tourist's residential or mailing address */
    @Column(length = 255)
    private String address;

    /**
     * Bookings: all hotel bookings made by this tourist.
     * mappedBy indicates that HotelBooking owns the relationship.
     * CascadeType.ALL ensures bookings are saved/deleted with the tourist.
     * orphanRemoval removes bookings that are detached from the tourist.
     */
    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HotelBooking> bookings = new ArrayList<>();
}
