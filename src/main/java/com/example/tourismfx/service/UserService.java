package com.example.tourismfx.service;

import com.example.tourismfx.model.Tourist;
import com.example.tourismfx.model.User;
import com.example.tourismfx.repository.TouristRepository;
import com.example.tourismfx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

/**
 * UserService.java - User Management and Authentication Service
 * ================================================================
 * Handles user registration, authentication, and profile management.
 * 
 * Implements Spring Security's UserDetailsService interface to integrate
 * with the authentication system. When a user logs in, Spring Security
 * calls loadUserByUsername() to verify credentials.
 * 
 * Registration flow:
 *   1. Validate username and email are unique
 *   2. Hash the password with BCrypt
 *   3. Create User entity with ROLE_TOURIST
 *   4. Create linked Tourist profile
 *   5. Save both entities to the database
 */
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TouristRepository touristRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * loadUserByUsername - Required by Spring Security's UserDetailsService.
     * Called automatically during the login process to load user credentials.
     * 
     * Converts our User entity into Spring Security's UserDetails object,
     * which contains the username, hashed password, and granted authorities (roles).
     * 
     * @param username the username entered in the login form
     * @return UserDetails object for Spring Security authentication
     * @throws UsernameNotFoundException if no user exists with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Look up the user in the database by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        // Convert our User entity to Spring Security's UserDetails format
        // SimpleGrantedAuthority wraps the role string for authorization checks
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),       // account enabled flag
                true,                   // accountNonExpired
                true,                   // credentialsNonExpired
                true,                   // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }

    /**
     * Register a new tourist user account.
     * Creates both the User (for auth) and Tourist (for profile) entities.
     * 
     * @param username    desired login username
     * @param email       user's email for notifications
     * @param password    plain-text password (will be hashed)
     * @param fullName    user's display name
     * @param phoneNumber phone for M-Pesa payments (optional)
     * @param nationality tourist's country of origin
     * @return the created Tourist entity with linked User
     * @throws RuntimeException if username or email is already taken
     */
    @Transactional
    public Tourist registerTourist(String username, String email, String password,
                                   String fullName, String phoneNumber, String nationality) {
        // Check for duplicate username
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username '" + username + "' is already taken");
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email '" + email + "' is already registered");
        }

        // Create the User entity with hashed password
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // BCrypt hash
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setRole("ROLE_TOURIST"); // Default role for new registrations
        user.setEnabled(true);

        // Create the Tourist profile linked to the User
        Tourist tourist = new Tourist();
        tourist.setUser(user);
        tourist.setNationality(nationality);

        // Save tourist (cascades to save User due to CascadeType.ALL)
        return touristRepository.save(tourist);
    }

    /**
     * Find a user by username.
     * Used for profile viewing and updating.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find a tourist profile by username.
     * Used to load the full tourist profile after login.
     */
    public Optional<Tourist> findTouristByUsername(String username) {
        return touristRepository.findByUserUsername(username);
    }
}
