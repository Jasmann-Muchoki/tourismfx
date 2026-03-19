package com.example.tourismfx.repository;

import com.example.tourismfx.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * HotelRepository.java - Data Access Layer for Hotel Entity
 * ============================================================
 * Provides database operations for Hotel entities including
 * search and filtering capabilities for the hotel listing page.
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /**
     * Find all active hotels (not hidden by admin).
     * Used to populate the public hotel listing page.
     * 
     * @return list of hotels where active = true
     */
    List<Hotel> findByActiveTrue();

    /**
     * Find hotels by location (city/region).
     * Case-insensitive search using Spring Data's IgnoreCase keyword.
     * 
     * @param location the city or region to search for
     * @return list of matching active hotels
     */
    List<Hotel> findByLocationContainingIgnoreCaseAndActiveTrue(String location);

    /**
     * Find hotels by star rating (e.g., all 4-star hotels).
     * Used for the star-rating filter on the hotel listing page.
     * 
     * @param starRating the minimum star rating to filter by
     * @return list of hotels with the specified rating or higher
     */
    List<Hotel> findByStarRatingGreaterThanEqualAndActiveTrue(int starRating);

    /**
     * Search hotels by name (partial, case-insensitive match).
     * Powers the search bar on the hotel listing page.
     * 
     * @param name the search term to match against hotel names
     * @return list of matching active hotels
     */
    List<Hotel> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}
