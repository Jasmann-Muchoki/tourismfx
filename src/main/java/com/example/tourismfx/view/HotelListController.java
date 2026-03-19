package com.example.tourismfx.view;

import com.example.tourismfx.model.Hotel;
import com.example.tourismfx.model.Room;
import com.example.tourismfx.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * HotelListController.java - Hotel Listing & Detail Controller
 * ================================================================
 * Handles the public hotel browsing pages.
 * These pages are accessible without authentication (see SecurityConfig).
 * 
 * Features:
 *   - Browse all active hotels with card-based listing
 *   - Search hotels by name
 *   - Filter by location and star rating
 *   - View hotel details with available rooms
 * 
 * URL Mappings:
 *   - GET /hotels          : List all hotels (with optional search/filter)
 *   - GET /hotels/{id}     : View hotel details and rooms
 *   - GET / or /home       : Home page
 * Templates: templates/hotel_list.html, templates/hotel_detail.html, templates/home.html
 */
@Controller
public class HotelListController {

    @Autowired
    private HotelService hotelService;

    /**
     * Display the home/landing page.
     * Shows featured hotels and a search bar.
     * 
     * @param model the Spring MVC model
     * @return the home template name
     */
    @GetMapping({"/", "/home"})
    public String showHomePage(Model model) {
        // Load featured hotels (all active hotels for now)
        List<Hotel> featuredHotels = hotelService.getAllActiveHotels();
        model.addAttribute("hotels", featuredHotels);
        return "home"; // Renders templates/home.html
    }

    /**
     * Display the hotel listing page with search and filter support.
     * 
     * @param search   optional search term for hotel name
     * @param location optional location filter
     * @param rating   optional minimum star rating filter
     * @param model    the Spring MVC model
     * @return the hotel list template name
     */
    @GetMapping("/hotels")
    public String listHotels(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer rating,
            Model model) {

        List<Hotel> hotels;

        // Apply filters based on provided parameters
        if (search != null && !search.trim().isEmpty()) {
            // Search by hotel name
            hotels = hotelService.searchHotels(search);
            model.addAttribute("searchTerm", search);
        } else if (location != null && !location.trim().isEmpty()) {
            // Filter by location
            hotels = hotelService.getHotelsByLocation(location);
            model.addAttribute("locationFilter", location);
        } else if (rating != null) {
            // Filter by minimum star rating
            hotels = hotelService.getHotelsByMinRating(rating);
            model.addAttribute("ratingFilter", rating);
        } else {
            // No filter — show all active hotels
            hotels = hotelService.getAllActiveHotels();
        }

        model.addAttribute("hotels", hotels);
        return "hotel_list"; // Renders templates/hotel_list.html
    }

    /**
     * Display detailed information for a specific hotel.
     * Shows hotel details, available rooms, and a booking link.
     * 
     * @param id    the hotel ID from the URL path
     * @param model the Spring MVC model
     * @return the hotel detail template name
     */
    @GetMapping("/hotels/{id}")
    public String showHotelDetail(@PathVariable Long id, Model model) {
        // Find the hotel by ID
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with ID: " + id));

        // Load available rooms for this hotel
        List<Room> availableRooms = hotelService.getAvailableRooms(id);

        model.addAttribute("hotel", hotel);
        model.addAttribute("rooms", availableRooms);
        return "hotel_detail"; // Renders templates/hotel_detail.html
    }
}
