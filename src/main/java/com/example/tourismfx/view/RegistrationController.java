package com.example.tourismfx.view;

import com.example.tourismfx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * RegistrationController.java - User Registration Controller
 * =============================================================
 * Handles new tourist account registration.
 * 
 * Registration flow:
 *   1. Tourist fills out the registration form (GET /register)
 *   2. Form is submitted (POST /register)
 *   3. UserService validates and creates the account
 *   4. On success: redirect to login with success message
 *   5. On failure: show registration form with error message
 * 
 * URL Mappings:
 *   - GET  /register : Show registration form
 *   - POST /register : Process registration
 * Template: templates/registration.html
 */
@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    /**
     * Display the registration form.
     * 
     * @return the registration template name
     */
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "registration"; // Renders templates/registration.html
    }

    /**
     * Process the registration form submission.
     * Creates a new tourist account with the provided details.
     * 
     * @param username    desired login username
     * @param email       email address for notifications
     * @param password    chosen password (will be BCrypt hashed)
     * @param confirmPassword password confirmation (must match)
     * @param fullName    tourist's full name
     * @param phoneNumber phone number for M-Pesa (optional)
     * @param nationality country of origin
     * @param redirectAttributes for flash messages after redirect
     * @return redirect URL (login on success, register on failure)
     */
    @PostMapping("/register")
    public String processRegistration(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String fullName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam String nationality,
            RedirectAttributes redirectAttributes) {

        // Validate password confirmation matches
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match");
            return "redirect:/register";
        }

        // Validate password length
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Password must be at least 6 characters long");
            return "redirect:/register";
        }

        try {
            // Create the tourist account via UserService
            userService.registerTourist(username, email, password,
                    fullName, phoneNumber, nationality);

            // Registration successful — redirect to login with success message
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! Please log in with your new account.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            // Registration failed (duplicate username/email, validation error)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }
}
