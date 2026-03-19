package com.example.tourismfx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JpaConfig.java - JPA and Database Configuration
 * =================================================
 * Configures Spring Data JPA for the TourismFX application.
 * 
 * @EnableJpaRepositories: Activates Spring Data JPA repository scanning.
 *   It looks for interfaces extending JpaRepository in the specified package
 *   and automatically generates their implementations at runtime.
 * 
 * @EnableJpaAuditing: Enables automatic population of @CreatedDate and
 *   @LastModifiedDate fields on entities, useful for tracking when bookings
 *   and payments were created or updated.
 * 
 * @EnableTransactionManagement: Activates Spring's annotation-driven
 *   transaction management (@Transactional). This ensures database operations
 *   in services are atomic — either all succeed or all roll back.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.example.tourismfx.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    // Configuration is handled via annotations above.
    // Additional JPA customization (e.g., custom naming strategies,
    // entity listeners) can be added here as bean definitions.
}
