package com.example.tourismfx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * App.java - Main Application Entry Point
 * ========================================
 * This is the bootstrap class for the TourismFX application.
 * 
 * @SpringBootApplication combines three annotations:
 *   - @Configuration:        Marks this class as a source of bean definitions
 *   - @EnableAutoConfiguration: Tells Spring Boot to auto-configure beans based on classpath
 *   - @ComponentScan:        Scans the com.example.tourismfx package for Spring components
 * 
 * @EnableAsync allows asynchronous method execution (used for sending emails
 *   in the background without blocking the main request thread)
 * 
 * To run this application:
 *   1. Ensure MySQL is running and the database 'tourismfx_db' exists
 *   2. Run: mvn spring-boot:run
 *   3. Open browser: http://localhost:8080
 */
@SpringBootApplication
@EnableAsync
public class App {

    /**
     * Main method - the JVM entry point that launches the Spring Boot application.
     * SpringApplication.run() performs the following:
     *   1. Creates the Spring ApplicationContext
     *   2. Registers all beans and configurations
     *   3. Starts the embedded Tomcat server on port 8080
     *   4. Initializes the database schema via JPA/Hibernate
     *
     * @param args command-line arguments (can include --server.port=XXXX to change port)
     */
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
