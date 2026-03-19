-- =============================================================
-- data.sql - TourismFX Seed Data
-- =============================================================
-- Populates the database with sample data for development and testing.
--
-- Contains:
--   - Admin user account (admin/admin123)
--   - Sample tourist account (tourist/tourist123)
--   - Sample hotels in Kenya
--   - Sample rooms for each hotel
--
-- Passwords are BCrypt hashed. The raw passwords are:
--   admin:   admin123
--   tourist: tourist123
--
-- IMPORTANT: Change these passwords in production!
-- =============================================================

-- ===================== ADMIN USER =====================
-- Insert admin user (password: admin123, BCrypt hashed)
INSERT INTO users (username, email, password, full_name, phone_number, role, enabled)
VALUES ('admin', 'admin@tourismfx.com',
        '$2a$10$TFYzPHgHhVGFAYfZDCq1h.IVBl5OPIifF3rPPEKA.ZsLSqXfNHGCy',
        'System Administrator', '+254700000000', 'ROLE_ADMIN', TRUE)
ON DUPLICATE KEY UPDATE username=username;

-- ===================== TOURIST USER =====================
-- Insert sample tourist user (password: tourist123, BCrypt hashed)
INSERT INTO users (username, email, password, full_name, phone_number, role, enabled)
VALUES ('tourist', 'tourist@example.com',
        '$2a$10$XJKz5B2Tz2q5GQpH3vM.g.K2b/RZkF.ZBkNqFIIpQ3hKL3Yp5qPXq',
        'John Doe', '+254712345678', 'ROLE_TOURIST', TRUE)
ON DUPLICATE KEY UPDATE username=username;

-- Link tourist user to tourist profile
INSERT INTO tourists (user_id, nationality, passport_number, preferred_language, address)
SELECT id, 'Kenya', 'KE12345678', 'English', 'Nairobi, Kenya'
FROM users WHERE username = 'tourist'
ON DUPLICATE KEY UPDATE nationality=nationality;

-- ===================== SAMPLE HOTELS =====================

-- Hotel 1: Serena Hotel Nairobi (5-star)
INSERT INTO hotels (name, description, location, address, star_rating, image_url, contact_phone, contact_email, active)
VALUES ('Serena Hotel Nairobi',
        'A luxury 5-star hotel in the heart of Nairobi, offering world-class accommodation with stunning views of Uhuru Gardens. Features include a spa, infinity pool, fine dining restaurants, and a business center.',
        'Nairobi', 'Kenyatta Avenue, Nairobi CBD',
        5, '/images/hotel-1.jpg', '+254 20 2822000', 'nairobi@serenahotels.com', TRUE)
ON DUPLICATE KEY UPDATE name=name;

-- Hotel 2: Diani Reef Beach Resort (4-star)
INSERT INTO hotels (name, description, location, address, star_rating, image_url, contact_phone, contact_email, active)
VALUES ('Diani Reef Beach Resort',
        'A beautiful beachfront resort on the white sands of Diani Beach. Enjoy water sports, diving, snorkeling, and relaxation by the Indian Ocean. All-inclusive packages available.',
        'Diani Beach', 'Diani Beach Road, Kwale County',
        4, '/images/hotel-2.jpg', '+254 40 3203723', 'info@dianireef.com', TRUE)
ON DUPLICATE KEY UPDATE name=name;

-- Hotel 3: Mount Kenya Safari Club (5-star)
INSERT INTO hotels (name, description, location, address, star_rating, image_url, contact_phone, contact_email, active)
VALUES ('Mount Kenya Safari Club',
        'Nestled on the slopes of Mount Kenya, this exclusive safari club offers luxury cottages with breathtaking mountain views. Enjoy horseback riding, golf, and wildlife safaris.',
        'Nanyuki', 'Mount Kenya, Nanyuki',
        5, '/images/hotel-3.jpg', '+254 62 3102000', 'reservations@fairmont.com', TRUE)
ON DUPLICATE KEY UPDATE name=name;

-- Hotel 4: Mara Serena Safari Lodge (4-star)
INSERT INTO hotels (name, description, location, address, star_rating, image_url, contact_phone, contact_email, active)
VALUES ('Mara Serena Safari Lodge',
        'Set high on a bush-cloaked hill overlooking the Masai Mara. Watch the great wildebeest migration from your private balcony. Game drives and hot air balloon safaris available.',
        'Masai Mara', 'Masai Mara National Reserve',
        4, '/images/hotel-4.jpg', '+254 20 2842333', 'mara@serenahotels.com', TRUE)
ON DUPLICATE KEY UPDATE name=name;

-- Hotel 5: Sarova Whitesands (3-star)
INSERT INTO hotels (name, description, location, address, star_rating, image_url, contact_phone, contact_email, active)
VALUES ('Sarova Whitesands Beach Resort',
        'A popular coastal resort in Mombasa with direct beach access. Features multiple pools, kids club, water sports center, and diverse dining options. Great for family vacations.',
        'Mombasa', 'North Coast, Bamburi Beach, Mombasa',
        3, '/images/hotel-5.jpg', '+254 41 2548721', 'whitesands@sarovahotels.com', TRUE)
ON DUPLICATE KEY UPDATE name=name;

-- ===================== SAMPLE ROOMS =====================

-- Rooms for Serena Hotel Nairobi
INSERT INTO rooms (hotel_id, room_type, description, price_per_night, capacity, total_rooms, available_rooms, active)
SELECT id, 'Presidential Suite', 'Luxurious suite with panoramic city views, private lounge, king-size bed, marble bathroom, and butler service.', 450.00, 4, 2, 2, TRUE
FROM hotels WHERE name = 'Serena Hotel Nairobi'
ON DUPLICATE KEY UPDATE room_type=room_type;

INSERT INTO rooms (hotel_id, room_type, description, price_per_night, capacity, total_rooms, available_rooms, active)
SELECT id, 'Deluxe Room', 'Spacious room with city or garden views, queen-size bed, work desk, and en-suite bathroom.', 180.00, 2, 20, 18, TRUE
FROM hotels WHERE name = 'Serena Hotel Nairobi'
ON DUPLICATE KEY UPDATE room_type=room_type;

INSERT INTO rooms (hotel_id, room_type, description, price_per_night, capacity, total_rooms, available_rooms, active)
SELECT id, 'Standard Room', 'Comfortable room with modern amenities, double bed, Wi-Fi, and breakfast included.', 120.00, 2, 30, 25, TRUE
FROM hotels WHERE name = 'Serena Hotel Nairobi'
ON DUPLICATE KEY UPDATE room_type=room_type;

-- Rooms for Diani Reef Beach Resort
INSERT INTO rooms (hotel_id, room_type, description, price_per_night, capacity, total_rooms, available_rooms, active)
SELECT id, 'Ocean View Suite', 'Beachfront suite with private balcony overlooking the Indian Ocean, king-size bed, and mini bar.', 320.00, 3, 5, 4, TRUE
FROM hotels WHERE name = 'Diani Reef Beach Resort'
ON DUPLICATE KEY UPDATE room_type=room_type;

INSERT INTO rooms (hotel_id, room_type, description, price_per_night, capacity, total_rooms, available_rooms, active)
SELECT id, 'Garden Room', 'Tropical garden-facing room with modern decor, twin beds option, and pool access.', 150.00, 2, 25, 20, TRUE
FROM hotels WHERE name = 'Diani Reef Beach Resort'
ON DUPLICATE KEY UPDATE room_type=room_type;

-- Rooms for Mount Kenya Safari Club
INSERT INTO rooms (hotel_id, room_type, description, price_per_night, capacity, total_rooms, available_rooms, active)
SELECT id, 'Mountain View Cottage', 'Private cottage with Mt. Kenya views, fireplace, king-size bed, and private garden.', 380.00, 2, 8, 6, TRUE
FROM hotels WHERE name = 'Mount Kenya Safari Club'
ON DUPLICATE KEY UPDATE room_type=room_type;

INSERT INTO rooms (hotel_id, room_type, description, price_per_night, capacity, total_rooms, available_rooms, active)
SELECT id, 'Safari Room', 'Elegant room with African-inspired decor, queen-size bed, and views of the conservancy.', 220.00, 2, 15, 12, TRUE
FROM hotels WHERE name = 'Mount Kenya Safari Club'
ON DUPLICATE KEY UPDATE room_type=room_type;

-- Rooms for Mara Serena Safari Lodge
INSERT INTO rooms (hotel_id, room_type, description, price_per_night, capacity, total_rooms, available_rooms, active)
SELECT id, 'Bush Suite', 'Luxury suite overlooking the Mara savanna, king-size bed, private deck with wildlife views.', 350.00, 2, 4, 3, TRUE
FROM hotels WHERE name = 'Mara Serena Safari Lodge'
ON DUPLICATE KEY UPDATE room_type=room_type;

INSERT INTO rooms (hotel_id, room_type, description, price_per_night, capacity, total_rooms, available_rooms, active)
SELECT id, 'Safari Tent', 'Authentic tented camp experience with modern comforts, twin beds, and ensuite shower.', 200.00, 2, 10, 8, TRUE
FROM hotels WHERE name = 'Mara Serena Safari Lodge'
ON DUPLICATE KEY UPDATE room_type=room_type;

-- Rooms for Sarova Whitesands
INSERT INTO rooms (hotel_id, room_type, description, price_per_night, capacity, total_rooms, available_rooms, active)
SELECT id, 'Superior Room', 'Beach-facing room with balcony, double bed, air conditioning, and sea breeze.', 130.00, 2, 40, 35, TRUE
FROM hotels WHERE name = 'Sarova Whitesands Beach Resort'
ON DUPLICATE KEY UPDATE room_type=room_type;

INSERT INTO rooms (hotel_id, room_type, description, price_per_night, capacity, total_rooms, available_rooms, active)
SELECT id, 'Family Suite', 'Two-bedroom suite perfect for families, living area, kitchenette, and pool access.', 220.00, 5, 10, 8, TRUE
FROM hotels WHERE name = 'Sarova Whitesands Beach Resort'
ON DUPLICATE KEY UPDATE room_type=room_type;
