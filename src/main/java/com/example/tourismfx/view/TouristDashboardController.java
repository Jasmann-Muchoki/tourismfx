package com.example.tourismfx.view;

import com.example.tourismfx.model.HotelBooking;
import com.example.tourismfx.model.Receipt;
import com.example.tourismfx.model.Tourist;
import com.example.tourismfx.service.BookingService;
import com.example.tourismfx.service.ReceiptService;
import com.example.tourismfx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TouristDashboardController.java - Tourist Dashboard Controller
 * =================================================================
 * Displays the tourist's personal dashboard after login.
 * 
 * Shows:
 *   - Welcome message with tourist's name
 *   - Booking history with status indicators
 *   - Receipt download links for confirmed bookings
 *   - Quick links to browse hotels and make new bookings
 * 
 * The dashboard also serves as the default landing page after login
 * (configured as defaultSuccessUrl in SecurityConfig).
 * 
 * URL Mapping: GET /dashboard
 * Template: templates/tourist_dashboard.html
 */
@Controller
public class TouristDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ReceiptService receiptService;

    /**
     * Display the tourist dashboard.
     * Loads the tourist's profile, booking history, and available receipts.
     * 
     * For admin users, redirects to the admin dashboard instead.
     * 
     * @param authentication Spring Security's auth object (contains logged-in user info)
     * @param model the Spring MVC model for passing data to the template
     * @return the dashboard template name
     */
    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();

        // Check if the user is an admin — redirect to admin dashboard
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return "redirect:/admin/dashboard";
        }

        // Load the tourist profile
        Optional<Tourist> touristOpt = userService.findTouristByUsername(username);
        if (touristOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Tourist profile not found");
            return "tourist_dashboard";
        }

        Tourist tourist = touristOpt.get();

        // Load booking history for this tourist
        List<HotelBooking> bookings = bookingService.getBookingsForTourist(tourist.getId());

        // Build a map of booking ID → receipt (for download links)
        Map<Long, Receipt> receiptMap = new HashMap<>();
        for (HotelBooking booking : bookings) {
            receiptService.findByBookingId(booking.getId())
                    .ifPresent(receipt -> receiptMap.put(booking.getId(), receipt));
        }

        // Pass all data to the template
        model.addAttribute("tourist", tourist);
        model.addAttribute("user", tourist.getUser());
        model.addAttribute("bookings", bookings);
        model.addAttribute("receiptMap", receiptMap);

        return "tourist_dashboard"; // Renders templates/tourist_dashboard.html
    }
}
