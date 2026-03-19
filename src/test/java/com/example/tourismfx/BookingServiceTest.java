package com.example.tourismfx;

import com.example.tourismfx.model.Hotel;
import com.example.tourismfx.model.HotelBooking;
import com.example.tourismfx.model.Room;
import com.example.tourismfx.model.Tourist;
import com.example.tourismfx.model.User;
import com.example.tourismfx.repository.BookingRepository;
import com.example.tourismfx.service.BookingService;
import com.example.tourismfx.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BookingServiceTest.java - Unit Tests for BookingService
 * =========================================================
 * Tests the core booking business logic:
 *   - Creating bookings with price calculation
 *   - Validating dates and guest count
 *   - Confirming and cancelling bookings
 *   - Retrieving booking history
 * 
 * Uses Mockito to mock repository dependencies,
 * isolating the service logic from database operations.
 */
@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelService hotelService;

    @InjectMocks
    private BookingService bookingService;

    // Test fixtures — reusable test data
    private Tourist testTourist;
    private Room testRoom;
    private Hotel testHotel;
    private HotelBooking testBooking;

    /**
     * Set up test fixtures before each test method.
     * Creates sample User, Tourist, Hotel, Room, and Booking objects.
     */
    @BeforeEach
    void setUp() {
        // Create a test user
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test Tourist");
        testUser.setRole("ROLE_TOURIST");

        // Create a test tourist profile
        testTourist = new Tourist();
        testTourist.setId(1L);
        testTourist.setUser(testUser);
        testTourist.setNationality("Kenya");

        // Create a test hotel
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setLocation("Nairobi");
        testHotel.setStarRating(4);

        // Create a test room with $100/night, capacity 2, 5 available
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setHotel(testHotel);
        testRoom.setRoomType("Deluxe Room");
        testRoom.setPricePerNight(new BigDecimal("100.00"));
        testRoom.setCapacity(2);
        testRoom.setTotalRooms(10);
        testRoom.setAvailableRooms(5);
        testRoom.setActive(true);

        // Create a test booking
        testBooking = new HotelBooking();
        testBooking.setId(1L);
        testBooking.setBookingReference("BK-20240115-00001");
        testBooking.setTourist(testTourist);
        testBooking.setRoom(testRoom);
        testBooking.setCheckInDate(LocalDate.now().plusDays(7));
        testBooking.setCheckOutDate(LocalDate.now().plusDays(10));
        testBooking.setNumberOfGuests(2);
        testBooking.setTotalPrice(new BigDecimal("300.00"));
        testBooking.setStatus("PENDING");
    }

    /**
     * Test: Creating a valid booking should calculate price correctly.
     * 3 nights × $100/night = $300.00 total
     */
    @Test
    @DisplayName("Should create booking with correct price calculation")
    void testCreateBooking_ValidDates_CalculatesPrice() {
        // Arrange: set up mock to return the booking when saved
        when(bookingRepository.save(any(HotelBooking.class))).thenAnswer(invocation -> {
            HotelBooking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });
        when(bookingRepository.count()).thenReturn(0L);

        // Act: create booking for 3 nights
        LocalDate checkIn = LocalDate.now().plusDays(7);
        LocalDate checkOut = LocalDate.now().plusDays(10); // 3 nights

        HotelBooking result = bookingService.createBooking(
                testTourist, testRoom, checkIn, checkOut, 2, "No special requests");

        // Assert: price should be 3 nights × $100 = $300
        assertNotNull(result, "Booking should not be null");
        assertEquals(new BigDecimal("300.00"), result.getTotalPrice(),
                "Total price should be 300.00 for 3 nights at $100/night");
        assertEquals("PENDING", result.getStatus(),
                "New booking should have PENDING status");
        assertNotNull(result.getBookingReference(),
                "Booking should have a reference number");

        // Verify repository was called to save the booking
        verify(bookingRepository, times(1)).save(any(HotelBooking.class));
    }

    /**
     * Test: Creating a booking with past check-in date should throw exception.
     */
    @Test
    @DisplayName("Should reject booking with past check-in date")
    void testCreateBooking_PastCheckInDate_ThrowsException() {
        // Act & Assert: booking with yesterday as check-in should fail
        LocalDate pastDate = LocalDate.now().minusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.createBooking(
                        testTourist, testRoom, pastDate, checkOut, 1, null));

        assertTrue(exception.getMessage().contains("past"),
                "Error message should mention past date");
    }

    /**
     * Test: Check-out date must be after check-in date.
     */
    @Test
    @DisplayName("Should reject booking with check-out before check-in")
    void testCreateBooking_InvalidDateRange_ThrowsException() {
        // Act & Assert: check-out same as check-in should fail
        LocalDate sameDate = LocalDate.now().plusDays(5);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.createBooking(
                        testTourist, testRoom, sameDate, sameDate, 1, null));

        assertTrue(exception.getMessage().contains("after"),
                "Error message should mention check-out must be after check-in");
    }

    /**
     * Test: Guest count must not exceed room capacity.
     */
    @Test
    @DisplayName("Should reject booking exceeding room capacity")
    void testCreateBooking_ExceedsCapacity_ThrowsException() {
        // Act & Assert: 3 guests in a room with capacity 2 should fail
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.createBooking(
                        testTourist, testRoom,
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(3),
                        3, // exceeds capacity of 2
                        null));

        assertTrue(exception.getMessage().contains("capacity"),
                "Error message should mention room capacity");
    }

    /**
     * Test: Booking should fail when room has no availability.
     */
    @Test
    @DisplayName("Should reject booking when room is fully booked")
    void testCreateBooking_NoAvailability_ThrowsException() {
        // Arrange: set room as fully booked
        testRoom.setAvailableRooms(0);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.createBooking(
                        testTourist, testRoom,
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(3),
                        1, null));

        assertTrue(exception.getMessage().contains("fully booked"),
                "Error message should mention room is fully booked");
    }

    /**
     * Test: Confirming a booking should update status and decrement room count.
     */
    @Test
    @DisplayName("Should confirm booking and decrement room availability")
    void testConfirmBooking_UpdatesStatusAndRoomCount() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(HotelBooking.class))).thenReturn(testBooking);

        // Act
        HotelBooking result = bookingService.confirmBooking(1L);

        // Assert
        assertEquals("CONFIRMED", result.getStatus(),
                "Booking status should be CONFIRMED after confirmation");
        verify(hotelService, times(1))
                .decrementRoomAvailability(testRoom.getId());
    }

    /**
     * Test: Cancelling a confirmed booking should restore room availability.
     */
    @Test
    @DisplayName("Should cancel confirmed booking and restore room availability")
    void testCancelBooking_ConfirmedBooking_RestoresRoom() {
        // Arrange: set booking as confirmed
        testBooking.setStatus("CONFIRMED");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(HotelBooking.class))).thenReturn(testBooking);

        // Act
        HotelBooking result = bookingService.cancelBooking(1L);

        // Assert
        assertEquals("CANCELLED", result.getStatus(),
                "Booking status should be CANCELLED");
        verify(hotelService, times(1))
                .incrementRoomAvailability(testRoom.getId());
    }

    /**
     * Test: Retrieving bookings for a tourist should return ordered list.
     */
    @Test
    @DisplayName("Should retrieve tourist bookings ordered by date")
    void testGetBookingsForTourist_ReturnsOrderedList() {
        // Arrange
        HotelBooking booking2 = new HotelBooking();
        booking2.setId(2L);
        booking2.setBookingReference("BK-20240116-00002");

        List<HotelBooking> expectedBookings = Arrays.asList(testBooking, booking2);
        when(bookingRepository.findByTouristIdOrderByCreatedAtDesc(1L))
                .thenReturn(expectedBookings);

        // Act
        List<HotelBooking> result = bookingService.getBookingsForTourist(1L);

        // Assert
        assertEquals(2, result.size(), "Should return 2 bookings");
        assertEquals("BK-20240115-00001", result.get(0).getBookingReference(),
                "First booking should be the most recent");
    }
}
