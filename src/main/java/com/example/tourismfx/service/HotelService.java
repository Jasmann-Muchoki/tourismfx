package com.example.tourismfx.service;

import com.example.tourismfx.model.Hotel;
import com.example.tourismfx.model.Room;
import com.example.tourismfx.repository.HotelRepository;
import com.example.tourismfx.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * HotelService.java - Hotel and Room Management Service
 * ========================================================
 * Business logic layer for hotel and room operations.
 * 
 * Provides functionality for:
 *   - Listing and searching hotels (public, for tourists)
 *   - Managing hotels and rooms (admin operations)
 *   - Checking room availability for bookings
 * 
 * All database operations are wrapped in @Transactional to ensure
 * data consistency (e.g., room count updates are atomic).
 */
@Service
@Transactional(readOnly = true) // Default to read-only transactions for queries
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    // ==================== HOTEL QUERIES (Public) ====================

    /**
     * Get all active hotels for the public listing page.
     * Only returns hotels marked as active by admin.
     * 
     * @return list of active hotels
     */
    public List<Hotel> getAllActiveHotels() {
        return hotelRepository.findByActiveTrue();
    }

    /**
     * Search hotels by name (partial, case-insensitive).
     * Powers the search bar on the hotel listing page.
     * 
     * @param searchTerm the text to search for in hotel names
     * @return list of matching active hotels
     */
    public List<Hotel> searchHotels(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveHotels();
        }
        return hotelRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchTerm.trim());
    }

    /**
     * Filter hotels by location (city/region).
     * 
     * @param location the location to filter by
     * @return list of active hotels in the specified location
     */
    public List<Hotel> getHotelsByLocation(String location) {
        return hotelRepository.findByLocationContainingIgnoreCaseAndActiveTrue(location);
    }

    /**
     * Filter hotels by minimum star rating.
     * 
     * @param minRating the minimum star rating (1-5)
     * @return list of active hotels with rating >= minRating
     */
    public List<Hotel> getHotelsByMinRating(int minRating) {
        return hotelRepository.findByStarRatingGreaterThanEqualAndActiveTrue(minRating);
    }

    /**
     * Get a single hotel by ID with its rooms.
     * Used for the hotel detail page.
     * 
     * @param id the hotel ID
     * @return Optional containing the hotel if found
     */
    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    // ==================== ROOM QUERIES ====================

    /**
     * Get all available rooms for a specific hotel.
     * Only returns rooms that have at least 1 available unit.
     * 
     * @param hotelId the hotel to get rooms for
     * @return list of available room types
     */
    public List<Room> getAvailableRooms(Long hotelId) {
        return roomRepository.findByHotelIdAndAvailableRoomsGreaterThanAndActiveTrue(hotelId, 0);
    }

    /**
     * Get a room by its ID.
     * Used during booking creation to validate the selected room.
     * 
     * @param roomId the room ID
     * @return Optional containing the room if found
     */
    public Optional<Room> getRoomById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    // ==================== ADMIN OPERATIONS ====================

    /**
     * Get all hotels including inactive ones (admin view).
     * 
     * @return list of all hotels
     */
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    /**
     * Save or update a hotel (admin operation).
     * @Transactional overrides the class-level readOnly=true.
     * 
     * @param hotel the hotel entity to save
     * @return the saved hotel entity
     */
    @Transactional
    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    /**
     * Save or update a room (admin operation).
     * 
     * @param room the room entity to save
     * @return the saved room entity
     */
    @Transactional
    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    /**
     * Decrease available room count when a booking is confirmed.
     * Called by BookingService after successful payment.
     * 
     * @param roomId the room type that was booked
     * @throws RuntimeException if no rooms are available
     */
    @Transactional
    public void decrementRoomAvailability(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        if (room.getAvailableRooms() <= 0) {
            throw new RuntimeException("No available rooms of this type");
        }
        room.setAvailableRooms(room.getAvailableRooms() - 1);
        roomRepository.save(room);
    }

    /**
     * Increase available room count when a booking is cancelled.
     * Called by BookingService when a booking is cancelled.
     * 
     * @param roomId the room type to restore availability for
     */
    @Transactional
    public void incrementRoomAvailability(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        if (room.getAvailableRooms() < room.getTotalRooms()) {
            room.setAvailableRooms(room.getAvailableRooms() + 1);
            roomRepository.save(room);
        }
    }
}
