package com.example.tourismfx.repository;

import com.example.tourismfx.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository.java - Data Access Layer for User Entity
 * =========================================================
 * Spring Data JPA repository that provides CRUD operations for User entities.
 * 
 * By extending JpaRepository<User, Long>, this interface automatically gets:
 *   - save(User)       : Create or update a user
 *   - findById(Long)   : Find user by primary key
 *   - findAll()        : Retrieve all users
 *   - deleteById(Long) : Delete a user by ID
 *   - count()          : Count total users
 *   - And many more...
 * 
 * Custom query methods below use Spring Data's method-name-to-query derivation:
 *   findByUsername → SELECT * FROM users WHERE username = ?
 *   findByEmail    → SELECT * FROM users WHERE email = ?
 * 
 * No implementation class is needed — Spring generates it at runtime.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username (used during login authentication).
     * Returns Optional to safely handle the case where no user exists.
     * 
     * @param username the login username to search for
     * @return Optional containing the User if found, or empty if not
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email address (used for duplicate checking
     * during registration and for sending notifications).
     * 
     * @param email the email address to search for
     * @return Optional containing the User if found, or empty if not
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a username is already taken (used during registration
     * to prevent duplicate usernames).
     * 
     * @param username the username to check
     * @return true if a user with this username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email is already registered (used during registration
     * to prevent duplicate email addresses).
     * 
     * @param email the email to check
     * @return true if a user with this email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
