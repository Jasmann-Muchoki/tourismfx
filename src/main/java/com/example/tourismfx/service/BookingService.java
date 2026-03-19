package com.example.tourismfx.service;

import com.example.tourismfx.model.HotelBooking;
import com.example.tourismfx.model.Room;
import com.example.tourismfx.model.Tourist;
import com.example.tourismfx.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * BookingService.java - Booking Management Service
 * ===================================================
 * Core business logic for creating, managing, and tracking hotel bookings.
 * 
 * Booking creation flow:
 *   1. Tourist selects a hotel and room type
 *   2. Tourist enters check-in/out dates and guest count
 *   3. System calculates total price (nights × price per night)
 *   4. System generates unique booking reference
 *   5. Booking is saved with PENDING status
 *   6. Tourist is redirected to payment page
 * 
 * After payment:
 *   7. Booking status updated to CONFIRMED
 *   8. Room availability decremented
 *   9. Receipt generated and email sent
 */
@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HotelService hotelService;

    /**
     * Create a new hotel booking.
     * Validates dates, calculates price, generates reference, and saves.
     * 
     * @param tourist       the tourist making the booking
     * @param room          the selected room type
     * @param checkInDate   desired check-in date
     * @param checkOutDate  desired check-out date
     * @param numberOfGuests number of guests
     * @param specialRequests any special requests from the tourist
     * @return the created booking entity
     * @throws RuntimeException if dates are invalid or room unavailable
     */
    @Transactional
    public HotelBooking createBooking(Tourist tourist, Room room,
                                       LocalDate checkInDate, LocalDate checkOutDate,
                                       int numberOfGuests, String specialRequests) {
        // Validate check-in date is not in the past
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Check-in date cannot be in the past");
        }

        // Validate check-out is after check-in
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }

        // Validate guest count doesn't exceed room capacity
        if (numberOfGuests > room.getCapacity()) {
            throw new RuntimeException("Number of guests exceeds room capacity of " + room.getCapacity());
        }

        // Validate room availability
        if (room.getAvailableRooms() <= 0) {
            throw new RuntimeException("Sorry, this room type is fully booked");
        }

        // Calculate total price: number of nights × price per night
        long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        BigDecimal totalPrice = room.getPricePerNight()
                .multiply(BigDecimal.valueOf(numberOfNights));

        // Generate unique booking reference: BK-YYYYMMDD-XXXXX
        String bookingReference = generateBookingReference();

        // Create and populate the booking entity
        HotelBooking booking = new HotelBooking();
        booking.setBookingReference(bookingReference);
        booking.setTourist(tourist);
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setNumberOfGuests(numberOfGuests);
        booking.setTotalPrice(totalPrice);
        booking.setStatus("PENDING"); // Awaiting payment
        booking.setSpecialRequests(specialRequests);

        // Save the booking to the database
        return bookingRepository.save(booking);
    }

    /**
     * Confirm a booking after successful payment.
     * Updates status and decrements room availability.
     * 
     * @param bookingId the ID of the booking to confirm
     * @return the updated booking entity
     */
    @Transactional
    public HotelBooking confirmBooking(Long bookingId) {
        HotelBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        // Update booking status to CONFIRMED
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        // Decrement room availability (one less room of this type available)
        hotelService.decrementRoomAvailability(booking.getRoom().getId());

        return booking;
    }

    /**
     * Cancel a booking and restore room availability.
     * 
     * @param bookingId the ID of the booking to cancel
     * @return the updated booking entity
     */
    @Transactional
    public HotelBooking cancelBooking(Long bookingId) {
        HotelBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        // Only PENDING or CONFIRMED bookings can be cancelled
        if ("CANCELLED".equals(booking.getStatus()) || "COMPLETED".equals(booking.getStatus())) {
            throw new RuntimeException("Cannot cancel a " + booking.getStatus() + " booking");
        }

        // If booking was confirmed, restore room availability
        if ("CONFIRMED".equals(booking.getStatus())) {
            hotelService.incrementRoomAvailability(booking.getRoom().getId());
        }

        booking.setStatus("CANCELLED");
        return bookingRepository.save(booking);
    }

    /**
     * Get all bookings for a specific tourist.
     * Used on the Tourist Dashboard to display booking history.
     * 
     * @param touristId the tourist's ID
     * @return list of bookings ordered by creation date (newest first)
     */
    public List<HotelBooking> getBookingsForTourist(Long touristId) {
        return bookingRepository.findByTouristIdOrderByCreatedAtDesc(touristId);
    }

    /**
     * Find a booking by its unique reference number.
     * 
     * @param reference the booking reference (e.g., "BK-20240115-00042")
     * @return Optional containing the booking if found
     */
    public Optional<HotelBooking> findByReference(String reference) {
        return bookingRepository.findByBookingReference(reference);
    }

    /**
     * Find a booking by its ID.
     */
    public Optional<HotelBooking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    /**
     * Get all bookings (admin view).
     * 
     * @return all bookings ordered by creation date
     */
    public List<HotelBooking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Generate a unique booking reference number.
     * Format: BK-YYYYMMDD-XXXXX where XXXXX is a sequential number.
     * 
     * Example: BK-20240115-00042
     * 
     * @return the generated booking reference string
     */
    private String generateBookingReference() {
        // Get current date formatted as YYYYMMDD
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Get total booking count for sequential numbering
        long count = bookingRepository.count() + 1;

        // Combine into reference format: BK-YYYYMMDD-XXXXX
        return String.format("BK-%s-%05d", datePart, count);
    }
}
