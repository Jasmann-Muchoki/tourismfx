package com.example.tourismfx.repository;

import com.example.tourismfx.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * RoomRepository.java - Data Access Layer for Room Entity
 * ==========================================================
 * Provides database operations for hotel Room entities.
 * Used to find available rooms for booking and manage room inventory.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Find all active rooms for a specific hotel.
     * Used on the hotel detail page to show available room types.
     * 
     * @param hotelId the hotel to find rooms for
     * @return list of active rooms in the hotel
     */
    List<Room> findByHotelIdAndActiveTrue(Long hotelId);

    /**
     * Find rooms with availability (at least 1 room of that type available).
     * Used during booking to only show rooms that can be reserved.
     * 
     * @param hotelId the hotel to search in
     * @return list of rooms with availableRooms > 0
     */
    List<Room> findByHotelIdAndAvailableRoomsGreaterThanAndActiveTrue(Long hotelId, int minAvailable);
}
