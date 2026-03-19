package com.example.tourismfx.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * User.java - Base User Entity
 * =============================
 * Represents a registered user in the TourismFX system.
 * This entity handles authentication and authorization.
 * 
 * Users can have one of two roles:
 *   - ROLE_TOURIST: Can browse hotels, make bookings, and process payments
 *   - ROLE_ADMIN:   Can manage hotels, rooms, view all bookings and reports
 * 
 * The password is stored as a BCrypt hash for security (see SecurityConfig).
 * 
 * Table: users
 * Relationships:
 *   - One User can have one Tourist profile (1:1 via Tourist entity)
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class) // Enables @CreatedDate auto-population
@Data                   // Lombok: generates getters, setters, toString, equals, hashCode
@NoArgsConstructor      // Lombok: generates no-args constructor (required by JPA)
@AllArgsConstructor     // Lombok: generates all-args constructor for convenience
public class User {

    /** Primary key: auto-incremented unique identifier for each user */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username: unique login identifier, must be 3-50 characters */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /** Email: unique, validated email address for notifications and receipts */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    /** Password: BCrypt-hashed password (never stored in plain text) */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    /** Full name: displayed in the UI and on receipts */
    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /** Phone number: used for M-Pesa payments (format: 254XXXXXXXXX) */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * Role: determines the user's access level in the application.
     * Stored as a string (e.g., "ROLE_TOURIST", "ROLE_ADMIN").
     * Spring Security uses this value for authorization decisions.
     */
    @Column(nullable = false, length = 20)
    private String role = "ROLE_TOURIST"; // Default role for new registrations

    /** Account status: allows admin to enable/disable user accounts */
    @Column(nullable = false)
    private boolean enabled = true;

    /** Timestamp: automatically set when the user account is created */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
