package com.example.tourismfx.view;

import com.example.tourismfx.model.HotelBooking;
import com.example.tourismfx.model.Room;
import com.example.tourismfx.model.Tourist;
import com.example.tourismfx.service.BookingService;
import com.example.tourismfx.service.HotelService;
import com.example.tourismfx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * BookingController.java - Hotel Booking Controller
 * =====================================================
 * Handles the booking creation flow for tourists.
 * 
 * Booking flow:
 *   1. Tourist clicks "Book Now" on a room (GET /bookings/new?roomId=X)
 *   2. Booking form shown with room details and date pickers
 *   3. Tourist fills dates, guest count, and submits (POST /bookings/create)
 *   4. System validates and creates a PENDING booking
 *   5. Tourist is redirected to the payment page
 * 
 * All booking endpoints require authentication (ROLE_TOURIST or ROLE_ADMIN).
 * 
 * URL Mappings:
 *   - GET  /bookings/new           : Show booking form
 *   - POST /bookings/create        : Create a new booking
 *   - GET  /bookings/{id}          : View booking details
 *   - POST /bookings/{id}/cancel   : Cancel a booking
 * Template: templates/booking.html
 */
@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserService userService;

    /**
     * Show the booking form for a specific room.
     * Pre-populates room details and sets minimum dates.
     * 
     * @param roomId the room to book (from query parameter)
     * @param model  the Spring MVC model
     * @return the booking form template
     */
    @GetMapping("/new")
    public String showBookingForm(@RequestParam Long roomId, Model model) {
        // Load the room details to display on the form
        Room room = hotelService.getRoomById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));

        model.addAttribute("room", room);
        model.addAttribute("hotel", room.getHotel());
        model.addAttribute("minDate", LocalDate.now()); // Prevent past dates
        return "booking"; // Renders templates/booking.html
    }

    /**
     * Process the booking form submission.
     * Creates a new booking and redirects to payment.
     * 
     * @param roomId          the selected room ID
     * @param checkInDate     desired check-in date (format: yyyy-MM-dd)
     * @param checkOutDate    desired check-out date
     * @param numberOfGuests  number of guests
     * @param specialRequests any special requests from the tourist
     * @param authentication  Spring Security auth (to identify the tourist)
     * @param redirectAttributes for flash messages
     * @return redirect to payment page on success, or back to form on error
     */
    @PostMapping("/create")
    public String createBooking(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam int numberOfGuests,
            @RequestParam(required = false) String specialRequests,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            // Get the logged-in tourist's profile
            String username = authentication.getName();
            Tourist tourist = userService.findTouristByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Tourist profile not found"));

            // Get the selected room
            Room room = hotelService.getRoomById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            // Create the booking (validates dates, calculates price, generates reference)
            HotelBooking booking = bookingService.createBooking(
                    tourist, room, checkInDate, checkOutDate,
                    numberOfGuests, specialRequests);

            // Redirect to payment page with the new booking ID
            redirectAttributes.addFlashAttribute("successMessage",
                    "Booking created! Reference: " + booking.getBookingReference()
                            + ". Please complete payment.");
            return "redirect:/payments/checkout/" + booking.getId();

        } catch (RuntimeException e) {
            // Booking creation failed — show error on the form
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bookings/new?roomId=" + roomId;
        }
    }

    /**
     * View booking details page.
     * 
     * @param id    the booking ID
     * @param model the Spring MVC model
     * @return the booking detail template
     */
    @GetMapping("/{id}")
    public String viewBooking(@PathVariable Long id, Model model) {
        HotelBooking booking = bookingService.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));

        model.addAttribute("booking", booking);
        model.addAttribute("room", booking.getRoom());
        model.addAttribute("hotel", booking.getRoom().getHotel());
        return "booking_detail"; // Renders templates/booking_detail.html
    }

    /**
     * Cancel a booking.
     * Only PENDING and CONFIRMED bookings can be cancelled.
     * 
     * @param id the booking ID to cancel
     * @param redirectAttributes for flash messages
     * @return redirect to tourist dashboard
     */
    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard";
    }
}
