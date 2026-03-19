package com.example.tourismfx.repository;

import com.example.tourismfx.model.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * TouristRepository.java - Data Access Layer for Tourist Entity
 * ================================================================
 * Provides database operations for Tourist profiles.
 * Links tourist-specific data (nationality, passport) to user accounts.
 * 
 * Inherits all standard CRUD operations from JpaRepository.
 */
@Repository
public interface TouristRepository extends JpaRepository<Tourist, Long> {

    /**
     * Find a tourist profile by their associated user ID.
     * Used after login to load the tourist's full profile with booking history.
     * 
     * @param userId the ID of the linked User account
     * @return Optional containing the Tourist if found
     */
    Optional<Tourist> findByUserId(Long userId);

    /**
     * Find a tourist profile by their associated username.
     * Convenience method that joins through the User entity.
     * 
     * @param username the username of the linked User account
     * @return Optional containing the Tourist if found
     */
    Optional<Tourist> findByUserUsername(String username);
}
