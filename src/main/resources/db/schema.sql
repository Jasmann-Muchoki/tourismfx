-- =============================================================
-- schema.sql - TourismFX Database Schema
-- =============================================================
-- This script creates the MySQL database tables for the
-- TourismFX Tourism Docket Management System.
--
-- Note: JPA/Hibernate can auto-create tables via spring.jpa.hibernate.ddl-auto=update
-- This file serves as documentation and for manual setup if needed.
--
-- Execution order matters due to foreign key dependencies:
--   1. users       (no dependencies)
--   2. tourists    (depends on users)
--   3. hotels      (no dependencies)
--   4. rooms       (depends on hotels)
--   5. hotel_bookings (depends on tourists, rooms)
--   6. payments    (depends on hotel_bookings)
--   7. receipts    (depends on payments, hotel_bookings)
-- =============================================================

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS tourismfx_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tourismfx_db;

-- =============================================================
-- 1. USERS TABLE - Authentication and authorization
-- =============================================================
-- Stores login credentials and basic user info.
-- Roles: ROLE_TOURIST (default), ROLE_ADMIN
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,           -- BCrypt hashed password
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_TOURIST',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================
-- 2. TOURISTS TABLE - Tourist profile extension
-- =============================================================
-- Links to users table for tourism-specific data.
CREATE TABLE IF NOT EXISTS tourists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    nationality VARCHAR(100),
    passport_number VARCHAR(50),
    preferred_language VARCHAR(50) DEFAULT 'English',
    address TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================
-- 3. HOTELS TABLE - Hotel/accommodation properties
-- =============================================================
CREATE TABLE IF NOT EXISTS hotels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    location VARCHAR(200) NOT NULL,
    address TEXT,
    star_rating INT NOT NULL DEFAULT 3,
    image_url VARCHAR(500),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_location (location),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================
-- 4. ROOMS TABLE - Room types within hotels
-- =============================================================
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    room_type VARCHAR(100) NOT NULL,
    description TEXT,
    price_per_night DECIMAL(10, 2) NOT NULL,
    capacity INT NOT NULL DEFAULT 2,
    total_rooms INT NOT NULL DEFAULT 1,
    available_rooms INT NOT NULL DEFAULT 1,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
    INDEX idx_hotel_id (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================
-- 5. HOTEL_BOOKINGS TABLE - Reservation records
-- =============================================================
CREATE TABLE IF NOT EXISTS hotel_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(50) NOT NULL UNIQUE,
    tourist_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_guests INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    special_requests TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tourist_id) REFERENCES tourists(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    INDEX idx_booking_ref (booking_reference),
    INDEX idx_tourist_id (tourist_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================
-- 6. PAYMENTS TABLE - Payment transaction records
-- =============================================================
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    payment_method VARCHAR(20) NOT NULL,      -- PAYPAL or MPESA
    transaction_id VARCHAR(200),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    gateway_response TEXT,
    payer_email VARCHAR(200),
    phone_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (booking_id) REFERENCES hotel_bookings(id),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================
-- 7. RECEIPTS TABLE - Generated receipt records
-- =============================================================
CREATE TABLE IF NOT EXISTS receipts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    payment_id BIGINT NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL,
    pdf_file_path VARCHAR(500),
    email_sent BOOLEAN NOT NULL DEFAULT FALSE,
    sent_to_email VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(id),
    FOREIGN KEY (booking_id) REFERENCES hotel_bookings(id),
    INDEX idx_receipt_number (receipt_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
