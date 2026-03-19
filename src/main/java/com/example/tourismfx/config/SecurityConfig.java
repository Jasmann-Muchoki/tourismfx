package com.example.tourismfx.config;

import com.example.tourismfx.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig.java - Spring Security Configuration
 * =====================================================
 * Configures authentication and authorization for the TourismFX application.
 * 
 * Security rules:
 *   - Public pages: home, login, registration, hotel listing, CSS/JS/images
 *   - Tourist pages: booking, payment, receipt (require ROLE_TOURIST)
 *   - Admin pages: admin dashboard (require ROLE_ADMIN)
 *   - All other pages require authentication
 * 
 * Uses BCrypt password hashing for secure credential storage.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * PasswordEncoder bean: BCrypt is an adaptive hashing function that
     * automatically handles salt generation and is resistant to brute-force attacks.
     * Strength factor of 10 (default) provides a good balance of security and speed.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * SecurityFilterChain: defines URL-level access control rules and
     * configures the login/logout behavior for the application.
     * 
     * @param http the HttpSecurity builder for configuring web-based security
     * @return the configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Authorization rules: define which URLs require which roles
            .authorizeRequests()
                // Public resources accessible without authentication
                .antMatchers("/", "/home", "/login", "/register",
                             "/hotels", "/hotels/**",
                             "/css/**", "/js/**", "/images/**",
                             "/webjars/**", "/error").permitAll()
                // Admin-only endpoints require ADMIN role
                .antMatchers("/admin/**").hasRole("ADMIN")
                // Tourist booking/payment pages require TOURIST role
                .antMatchers("/bookings/**", "/payments/**", "/receipts/**")
                    .hasAnyRole("TOURIST", "ADMIN")
                // All other requests require authentication
                .anyRequest().authenticated()
            .and()
            // Login configuration: custom login page with redirect on success
            .formLogin()
                .loginPage("/login")                    // Custom login page URL
                .loginProcessingUrl("/login")           // Form POST target
                .defaultSuccessUrl("/dashboard", true)  // Redirect after login
                .failureUrl("/login?error=true")        // Redirect on failure
                .permitAll()
            .and()
            // Logout configuration: clear session and redirect to home
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)            // Clear session data
                .deleteCookies("JSESSIONID")            // Remove session cookie
                .permitAll()
            .and()
            // Exception handling: redirect to login if access is denied
            .exceptionHandling()
                .accessDeniedPage("/access-denied");

        return http.build();
    }
}
