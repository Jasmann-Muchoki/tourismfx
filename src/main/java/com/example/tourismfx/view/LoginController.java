package com.example.tourismfx.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * LoginController.java - Login Page Controller
 * ===============================================
 * Handles the login page display and authentication feedback.
 * 
 * The actual authentication is handled by Spring Security's
 * UsernamePasswordAuthenticationFilter (configured in SecurityConfig).
 * This controller only serves the login page template.
 * 
 * URL Mapping: GET /login
 * Template: templates/login.html
 * 
 * Query parameters:
 *   - error=true  : Displayed after failed login attempt
 *   - logout=true : Displayed after successful logout
 */
@Controller
public class LoginController {

    /**
     * Display the login page.
     * Adds error/success messages based on query parameters.
     * 
     * @param error  present if login failed (wrong credentials)
     * @param logout present if user just logged out
     * @param model  the Spring MVC model for passing data to the template
     * @return the login template name
     */
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        // Add error message if login failed
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password. Please try again.");
        }

        // Add success message if user just logged out
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }

        return "login"; // Renders templates/login.html
    }

    /**
     * Handle the access denied page (when user lacks required role).
     * 
     * @return the access-denied template name
     */
    @GetMapping("/access-denied")
    public String showAccessDenied() {
        return "access-denied"; // Renders templates/access-denied.html
    }
}
