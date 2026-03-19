package com.example.tourismfx.repository;

import com.example.tourismfx.model.HotelBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BookingRepository.java - Data Access Layer for HotelBooking Entity
 * =====================================================================
 * Provides database operations for hotel booking records.
 * Supports querying by tourist, status, and booking reference.
 */
@Repository
public interface BookingRepository extends JpaRepository<HotelBooking, Long> {

    /**
     * Find all bookings for a specific tourist, ordered by creation date.
     * Used on the Tourist Dashboard to show booking history.
     * 
     * @param touristId the tourist whose bookings to retrieve
     * @return list of bookings, newest first
     */
    List<HotelBooking> findByTouristIdOrderByCreatedAtDesc(Long touristId);

    /**
     * Find a booking by its unique reference number.
     * Used for looking up bookings from receipt or customer inquiry.
     * 
     * @param bookingReference the unique booking reference (e.g., "BK-20240115-00042")
     * @return Optional containing the booking if found
     */
    Optional<HotelBooking> findByBookingReference(String bookingReference);

    /**
     * Find all bookings with a specific status.
     * Used by admin dashboard for filtering (e.g., show all PENDING bookings).
     * 
     * @param status the booking status to filter by
     * @return list of bookings with the specified status
     */
    List<HotelBooking> findByStatus(String status);

    /**
     * Find all bookings (admin view), ordered by creation date.
     * Used on the Admin Dashboard to view all reservations.
     * 
     * @return all bookings, newest first
     */
    List<HotelBooking> findAllByOrderByCreatedAtDesc();
}
