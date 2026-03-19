package com.example.tourismfx.view;

import com.example.tourismfx.model.Hotel;
import com.example.tourismfx.model.HotelBooking;
import com.example.tourismfx.model.Room;
import com.example.tourismfx.service.BookingService;
import com.example.tourismfx.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * AdminDashboardController.java - Admin Dashboard Controller
 * =============================================================
 * Provides the admin interface for managing hotels, rooms, and bookings.
 * All endpoints require ROLE_ADMIN (enforced by SecurityConfig).
 * 
 * Features:
 *   - View all bookings with status filtering
 *   - Manage hotels (add, edit, activate/deactivate)
 *   - Manage rooms (add, edit pricing and availability)
 *   - View booking statistics
 * 
 * URL Mappings: /admin/**
 * Templates: templates/admin_dashboard.html
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private HotelService hotelService;

    /**
     * Display the admin dashboard with overview statistics.
     * Shows recent bookings and hotel management links.
     * 
     * @param model the Spring MVC model
     * @return the admin dashboard template
     */
    @GetMapping({"", "/dashboard"})
    public String showDashboard(Model model) {
        // Load all bookings for the admin overview
        List<HotelBooking> allBookings = bookingService.getAllBookings();
        List<Hotel> allHotels = hotelService.getAllHotels();

        // Calculate statistics for the dashboard cards
        long totalBookings = allBookings.size();
        long confirmedBookings = allBookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus())).count();
        long pendingBookings = allBookings.stream()
                .filter(b -> "PENDING".equals(b.getStatus())).count();
        BigDecimal totalRevenue = allBookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .map(HotelBooking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Pass data to the template
        model.addAttribute("bookings", allBookings);
        model.addAttribute("hotels", allHotels);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("totalRevenue", totalRevenue);

        return "admin_dashboard"; // Renders templates/admin_dashboard.html
    }

    /**
     * Add a new hotel (POST from admin form).
     * 
     * @param name        hotel name
     * @param location    hotel location/city
     * @param description hotel description
     * @param starRating  star rating (1-5)
     * @param contactPhone hotel phone number
     * @param contactEmail hotel email
     * @param redirectAttributes for flash messages
     * @return redirect to admin dashboard
     */
    @PostMapping("/hotels/add")
    public String addHotel(@RequestParam String name,
                           @RequestParam String location,
                           @RequestParam(required = false) String description,
                           @RequestParam int starRating,
                           @RequestParam(required = false) String contactPhone,
                           @RequestParam(required = false) String contactEmail,
                           RedirectAttributes redirectAttributes) {
        try {
            Hotel hotel = new Hotel();
            hotel.setName(name);
            hotel.setLocation(location);
            hotel.setDescription(description);
            hotel.setStarRating(starRating);
            hotel.setContactPhone(contactPhone);
            hotel.setContactEmail(contactEmail);
            hotel.setActive(true);

            hotelService.saveHotel(hotel);
            redirectAttributes.addFlashAttribute("successMessage", "Hotel added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding hotel: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    /**
     * Add a room to an existing hotel (POST from admin form).
     * 
     * @param hotelId       the hotel to add the room to
     * @param roomType      room type name (e.g., "Deluxe Suite")
     * @param description   room description
     * @param pricePerNight price per night
     * @param capacity      maximum occupancy
     * @param totalRooms    total number of rooms of this type
     * @param redirectAttributes for flash messages
     * @return redirect to admin dashboard
     */
    @PostMapping("/rooms/add")
    public String addRoom(@RequestParam Long hotelId,
                          @RequestParam String roomType,
                          @RequestParam(required = false) String description,
                          @RequestParam BigDecimal pricePerNight,
                          @RequestParam int capacity,
                          @RequestParam int totalRooms,
                          RedirectAttributes redirectAttributes) {
        try {
            Hotel hotel = hotelService.getHotelById(hotelId)
                    .orElseThrow(() -> new RuntimeException("Hotel not found"));

            Room room = new Room();
            room.setHotel(hotel);
            room.setRoomType(roomType);
            room.setDescription(description);
            room.setPricePerNight(pricePerNight);
            room.setCapacity(capacity);
            room.setTotalRooms(totalRooms);
            room.setAvailableRooms(totalRooms); // All rooms initially available
            room.setActive(true);

            hotelService.saveRoom(room);
            redirectAttributes.addFlashAttribute("successMessage", "Room added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding room: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
