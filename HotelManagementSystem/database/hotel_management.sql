-- ==========================================================
-- HOTEL MANAGEMENT SYSTEM - DATABASE CREATION SCRIPT
-- MySQL 8.0+ Compatible Database Architecture
-- Clean Normalized Relational Schema with Constraints & Indexes
-- ==========================================================

DROP DATABASE IF EXISTS hotel_management;
CREATE DATABASE hotel_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hotel_management;

-- ----------------------------------------------------------
-- 1. Table: hotel_settings
-- ----------------------------------------------------------
CREATE TABLE hotel_settings (
    setting_id INT AUTO_INCREMENT PRIMARY KEY,
    hotel_name VARCHAR(150) NOT NULL,
    tagline VARCHAR(200),
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    website VARCHAR(150),
    currency VARCHAR(10) DEFAULT '$',
    tax_percentage DECIMAL(5, 2) DEFAULT 12.50,
    checkin_time TIME DEFAULT '14:00:00',
    checkout_time TIME DEFAULT '11:00:00',
    invoice_footer TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 2. Table: users (Authentication & RBAC)
-- Passwords hashed using BCrypt ($2a$10$...)
-- Default testing password for all sample accounts: password123
-- ----------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Manager', 'Receptionist', 'Accountant') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(30),
    status ENUM('Active', 'Inactive', 'Suspended') DEFAULT 'Active',
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 3. Table: staff (Employee records linked to users)
-- ----------------------------------------------------------
CREATE TABLE staff (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE,
    employee_code VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('Admin', 'Manager', 'Receptionist', 'Accountant') NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    address TEXT,
    salary DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    joining_date DATE NOT NULL,
    status ENUM('Active', 'Inactive', 'Terminated') DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_staff_code (employee_code),
    INDEX idx_staff_status (status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 4. Table: rooms (Hotel Inventory)
-- ----------------------------------------------------------
CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    room_type ENUM('Single', 'Double', 'Deluxe', 'Suite') NOT NULL,
    floor INT NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    capacity INT NOT NULL DEFAULT 2,
    status ENUM('Available', 'Reserved', 'Occupied', 'Cleaning', 'Maintenance') DEFAULT 'Available',
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_room_number (room_number),
    INDEX idx_room_type (room_type),
    INDEX idx_room_status (status),
    INDEX idx_room_floor (floor)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 5. Table: guests (Customer Master)
-- ----------------------------------------------------------
CREATE TABLE guests (
    guest_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    gender ENUM('Male', 'Female', 'Other') NOT NULL,
    date_of_birth DATE,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(100) NOT NULL,
    address TEXT,
    id_proof_type ENUM('Passport', 'Driving License', 'National ID', 'Voter ID', 'Aadhaar') NOT NULL,
    id_proof_number VARCHAR(100) NOT NULL,
    nationality VARCHAR(50) DEFAULT 'American',
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_guest_name (full_name),
    INDEX idx_guest_phone (phone),
    INDEX idx_guest_email (email),
    INDEX idx_guest_id_proof (id_proof_number)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 6. Table: reservations (Booking Records)
-- ----------------------------------------------------------
CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    guest_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_guests INT NOT NULL DEFAULT 1,
    reservation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Pending', 'Confirmed', 'Checked-In', 'Checked-Out', 'Cancelled') DEFAULT 'Confirmed',
    special_requests TEXT,
    created_by_user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_res_guest FOREIGN KEY (guest_id) REFERENCES guests(guest_id) ON DELETE RESTRICT,
    CONSTRAINT fk_res_room FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE RESTRICT,
    CONSTRAINT fk_res_user FOREIGN KEY (created_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_res_dates (check_in_date, check_out_date),
    INDEX idx_res_status (status),
    INDEX idx_res_guest (guest_id),
    INDEX idx_res_room (room_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 7. Table: check_ins (Reception Check-in Ledger)
-- ----------------------------------------------------------
CREATE TABLE check_ins (
    check_in_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL UNIQUE,
    check_in_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    room_key_issued VARCHAR(50) DEFAULT 'Key Card #1',
    verified_id_proof BOOLEAN DEFAULT TRUE,
    notes TEXT,
    processed_by_user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ci_res FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE RESTRICT,
    CONSTRAINT fk_ci_user FOREIGN KEY (processed_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_ci_time (check_in_time)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 8. Table: check_outs (Departure & Billing Handover)
-- ----------------------------------------------------------
CREATE TABLE check_outs (
    check_out_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL UNIQUE,
    check_out_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    room_inspected BOOLEAN DEFAULT TRUE,
    key_returned BOOLEAN DEFAULT TRUE,
    notes TEXT,
    processed_by_user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_co_res FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE RESTRICT,
    CONSTRAINT fk_co_user FOREIGN KEY (processed_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_co_time (check_out_time)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 9. Table: services (Catalog of hotel amenities)
-- ----------------------------------------------------------
CREATE TABLE services (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    category ENUM('Dining', 'Wellness', 'Housekeeping', 'Transport', 'Amenities', 'Other') DEFAULT 'Other',
    description VARCHAR(255),
    price DECIMAL(10, 2) NOT NULL,
    status ENUM('Active', 'Inactive') DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_service_name (service_name),
    INDEX idx_service_status (status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 10. Table: guest_services (Service line items during stay)
-- ----------------------------------------------------------
CREATE TABLE guest_services (
    guest_service_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    service_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    service_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gs_res FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE,
    CONSTRAINT fk_gs_service FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE RESTRICT,
    INDEX idx_gs_res (reservation_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 11. Table: invoices (Financial Invoicing Record)
-- ----------------------------------------------------------
CREATE TABLE invoices (
    invoice_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    reservation_id INT NOT NULL UNIQUE,
    guest_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_nights INT NOT NULL,
    room_charges DECIMAL(10, 2) NOT NULL,
    service_charges DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    additional_charges DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    tax_percentage DECIMAL(5, 2) NOT NULL DEFAULT 12.50,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(10, 2) NOT NULL,
    paid_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    balance_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    payment_status ENUM('Unpaid', 'Partially Paid', 'Paid', 'Refunded') DEFAULT 'Unpaid',
    payment_method ENUM('Cash', 'Card', 'UPI', 'Bank Transfer', 'Other') DEFAULT 'Card',
    invoice_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_res FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_guest FOREIGN KEY (guest_id) REFERENCES guests(guest_id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_room FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE RESTRICT,
    INDEX idx_inv_number (invoice_number),
    INDEX idx_inv_status (payment_status),
    INDEX idx_inv_date (invoice_date)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 12. Table: payments (Payment Transactions)
-- ----------------------------------------------------------
CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    reservation_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('Cash', 'Card', 'UPI', 'Bank Transfer', 'Other') NOT NULL,
    payment_status ENUM('Success', 'Pending', 'Failed', 'Refunded') DEFAULT 'Success',
    transaction_reference VARCHAR(100),
    payment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    processed_by_user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pay_inv FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id) ON DELETE CASCADE,
    CONSTRAINT fk_pay_res FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE RESTRICT,
    CONSTRAINT fk_pay_user FOREIGN KEY (processed_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_pay_inv (invoice_id),
    INDEX idx_pay_date (payment_date),
    INDEX idx_pay_status (payment_status)
) ENGINE=InnoDB;

-- ==========================================================
-- REALISTIC SEED DATA
-- Default password for all users is: password123
-- BCrypt Hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
-- ==========================================================

-- 1. Hotel Settings
INSERT INTO hotel_settings (hotel_name, tagline, address, phone, email, website, currency, tax_percentage, checkin_time, checkout_time, invoice_footer)
VALUES (
    'Grand Heritage Palace & Luxury Suites',
    'Excellence in Hospitality & Comfort',
    '742 Evergreen Grand Avenue, Metropolia City, NY 10001',
    '+1 (555) 019-2834',
    'reservations@grandheritagehotel.com',
    'https://www.grandheritagehotel.com',
    '$',
    12.50,
    '14:00:00',
    '11:00:00',
    'Thank you for staying at Grand Heritage Palace. We hope you enjoyed your time with us and look forward to welcoming you again soon!'
);

-- 2. Users (Admin, Manager, Receptionist, Accountant)
-- Password for all: password123
INSERT INTO users (username, password_hash, role, full_name, email, phone, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Admin', 'Alexander Vance (General Manager & Admin)', 'admin@grandheritagehotel.com', '+1 (555) 100-0001', 'Active'),
('manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Manager', 'Victoria Stirling (Operations Director)', 'manager@grandheritagehotel.com', '+1 (555) 100-0002', 'Active'),
('receptionist', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Receptionist', 'Sarah Jenkins (Head Receptionist)', 'receptionist@grandheritagehotel.com', '+1 (555) 100-0003', 'Active'),
('accountant', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Accountant', 'David Miller (Chief Financial Officer)', 'accountant@grandheritagehotel.com', '+1 (555) 100-0004', 'Active');

-- 3. Staff Members
INSERT INTO staff (user_id, employee_code, full_name, role, phone, email, address, salary, joining_date, status) VALUES
(1, 'EMP-1001', 'Alexander Vance', 'Admin', '+1 (555) 100-0001', 'admin@grandheritagehotel.com', '12 King Street, Metropolia City', 85000.00, '2020-01-15', 'Active'),
(2, 'EMP-1002', 'Victoria Stirling', 'Manager', '+1 (555) 100-0002', 'manager@grandheritagehotel.com', '45 Queen Terrace, Metropolia City', 68000.00, '2021-03-10', 'Active'),
(3, 'EMP-1003', 'Sarah Jenkins', 'Receptionist', '+1 (555) 100-0003', 'receptionist@grandheritagehotel.com', '88 Maple Boulevard, Metropolia City', 42000.00, '2022-06-01', 'Active'),
(4, 'EMP-1004', 'David Miller', 'Accountant', '+1 (555) 100-0004', 'accountant@grandheritagehotel.com', '104 Oakwood Court, Metropolia City', 58000.00, '2021-11-20', 'Active');

-- 4. Rooms (10 Rooms Across Floors 1-4, Types: Single, Double, Deluxe, Suite)
INSERT INTO rooms (room_number, room_type, floor, price_per_night, capacity, status, description) VALUES
('101', 'Single', 1, 89.00, 1, 'Available', 'Cozy standard single room with garden view, high-speed WiFi, and work desk.'),
('102', 'Single', 1, 89.00, 1, 'Reserved', 'Standard single room, quiet side wing with courtyard view.'),
('103', 'Double', 1, 139.00, 2, 'Available', 'Spacious double bed room with premium mattress and modern ensuite bathroom.'),
('201', 'Double', 2, 149.00, 2, 'Occupied', 'Deluxe double room with city view, smart TV, mini-bar, and coffee station.'),
('202', 'Double', 2, 149.00, 2, 'Cleaning', 'Standard double room with balcony overlooking the central fountain.'),
('203', 'Deluxe', 2, 219.00, 3, 'Available', 'Luxury deluxe room with king bed, sofa lounge, and marble bathroom.'),
('301', 'Deluxe', 3, 239.00, 3, 'Reserved', 'Executive deluxe suite with panoramic skyline views and complimentary mini-bar.'),
('302', 'Deluxe', 3, 239.00, 3, 'Occupied', 'High-floor deluxe room with deep soaking bathtub and soundproof design.'),
('401', 'Suite', 4, 389.00, 4, 'Available', 'Presidential Penthouse Suite with master bedroom, private terrace, and jacuzzi.'),
('402', 'Suite', 4, 349.00, 4, 'Maintenance', 'Royal Grand Suite with conference dining room and premium butler service.');

-- 5. Guests
INSERT INTO guests (full_name, gender, date_of_birth, phone, email, address, id_proof_type, id_proof_number, nationality) VALUES
('Jonathan Sterling', 'Male', '1985-04-12', '+1 (555) 234-5678', 'jonathan.sterling@example.com', '542 Pine Valley Rd, Boston, MA', 'Passport', 'P98421045', 'American'),
('Elena Rostova', 'Female', '1992-09-24', '+1 (555) 876-5432', 'elena.rostova@example.com', '12 Nevsky Prospect, Seattle, WA', 'Driving License', 'DL-847291-WA', 'Canadian'),
('Marcus Aurelius Brody', 'Male', '1978-11-05', '+1 (555) 345-6789', 'marcus.brody@example.com', '789 Heritage Way, Chicago, IL', 'National ID', 'NID-7729103', 'American'),
('Sophia Chen-Wu', 'Female', '1995-02-18', '+1 (555) 901-2345', 'sophia.chen@example.com', '33 Bayview Crescent, San Francisco, CA', 'Passport', 'P33091824', 'Singaporean'),
('Dr. Arthur Pendelton', 'Male', '1968-07-30', '+1 (555) 456-7890', 'arthur.pendelton@example.com', '10 Downing Gate, Austin, TX', 'Driving License', 'DL-399104-TX', 'British');

-- 6. Services
INSERT INTO services (service_name, category, description, price, status) VALUES
('Gourmet Buffet Breakfast', 'Dining', 'Full continental & hot breakfast buffet with artisan bakery and barista coffee', 24.00, 'Active'),
('Express Laundry & Dry Cleaning', 'Housekeeping', 'Same-day laundry, washing, pressing and steam garment care', 18.00, 'Active'),
('Executive Room Service Meal', 'Dining', 'Chef-curated 3-course private room dining with wine pairing option', 45.00, 'Active'),
('Holistic Ayurvedic Spa & Massage', 'Wellness', '60-minute relaxing Swedish or deep-tissue therapeutic massage', 85.00, 'Active'),
('Luxury VIP Airport Chauffeur', 'Transport', 'Private Mercedes-Benz S-Class airport pickup or drop-off service', 65.00, 'Active'),
('Extra Deluxe Rollaway Bed', 'Amenities', 'Premium foam rollaway bed with hypoallergenic pillows and duvet', 30.00, 'Active'),
('High-Speed Premium Fiber Internet', 'Amenities', 'Dedicated 1Gbps ultra-low latency connection for streaming and conferences', 12.00, 'Active');

-- 7. Sample Reservations
-- Res 1: Confirmed upcoming (Jonathan Sterling in 102)
INSERT INTO reservations (reservation_id, guest_id, room_id, check_in_date, check_out_date, number_of_guests, status, special_requests, created_by_user_id)
VALUES (1, 1, 2, '2026-09-01', '2026-09-05', 1, 'Confirmed', 'Quiet room requested away from elevators. Late check-in around 18:00.', 3);

-- Res 2: Currently Checked-In (Elena Rostova in 201)
INSERT INTO reservations (reservation_id, guest_id, room_id, check_in_date, check_out_date, number_of_guests, status, special_requests, created_by_user_id)
VALUES (2, 2, 4, '2026-08-26', '2026-08-30', 2, 'Checked-In', 'Extra feather pillows and champagne on arrival.', 3);

-- Res 3: Currently Checked-In (Sophia Chen-Wu in 302)
INSERT INTO reservations (reservation_id, guest_id, room_id, check_in_date, check_out_date, number_of_guests, status, special_requests, created_by_user_id)
VALUES (3, 4, 8, '2026-08-27', '2026-08-29', 2, 'Checked-In', 'High floor skyline view preferred.', 3);

-- Res 4: Confirmed upcoming (Dr. Arthur Pendelton in 301)
INSERT INTO reservations (reservation_id, guest_id, room_id, check_in_date, check_out_date, number_of_guests, status, special_requests, created_by_user_id)
VALUES (4, 5, 7, '2026-09-02', '2026-09-06', 1, 'Confirmed', 'Airport pickup required at 15:30.', 3);

-- Res 5: Past Completed Check-Out (Marcus Aurelius Brody in 103)
INSERT INTO reservations (reservation_id, guest_id, room_id, check_in_date, check_out_date, number_of_guests, status, special_requests, created_by_user_id)
VALUES (5, 3, 3, '2026-08-20', '2026-08-24', 2, 'Checked-Out', 'Twin beds arrangement.', 3);

-- 8. Check-ins for active reservations
INSERT INTO check_ins (reservation_id, check_in_time, room_key_issued, verified_id_proof, notes, processed_by_user_id) VALUES
(2, '2026-08-26 14:15:00', 'Key Card #201-A', TRUE, 'Guest presented Driver License. Keys handed over.', 3),
(3, '2026-08-27 15:30:00', 'Key Card #302-A', TRUE, 'Passport verified. Welcome drink provided.', 3),
(5, '2026-08-20 14:00:00', 'Key Card #103-A', TRUE, 'Standard check-in completed.', 3);

-- 9. Check-out for completed reservation
INSERT INTO check_outs (reservation_id, check_out_time, room_inspected, key_returned, notes, processed_by_user_id) VALUES
(5, '2026-08-24 10:45:00', TRUE, TRUE, 'Room inspected without damage. Full bill settled via Credit Card.', 3);

-- 10. Guest Services Line Items
INSERT INTO guest_services (reservation_id, service_id, quantity, unit_price, total_price, service_date, notes) VALUES
(2, 1, 2, 24.00, 48.00, '2026-08-27 08:30:00', 'Buffet breakfast for 2 guests'),
(2, 3, 1, 45.00, 45.00, '2026-08-27 20:00:00', 'Room dining order - Salmon & Pinot Noir'),
(3, 4, 1, 85.00, 85.00, '2026-08-28 11:00:00', 'Holistic Spa relaxation therapy'),
(5, 1, 4, 24.00, 96.00, '2026-08-21 09:00:00', 'Daily breakfast during 4 night stay'),
(5, 2, 2, 18.00, 36.00, '2026-08-22 14:00:00', 'Laundry & pressing');

-- 11. Invoices
-- Completed Invoice for Reservation 5 (Marcus Brody)
-- 4 nights * $139 = $556 room charges + $132 services = $688 subtotal. Discount: $20. Net Subtotal: $668. Tax @ 12.5%: $83.50. Grand Total: $751.50. Paid: $751.50
INSERT INTO invoices (
    invoice_number, reservation_id, guest_id, room_id, check_in_date, check_out_date,
    number_of_nights, room_charges, service_charges, additional_charges, subtotal,
    discount, tax_percentage, tax_amount, grand_total, paid_amount, balance_amount,
    payment_status, payment_method, invoice_date
) VALUES (
    'INV-2026-0001', 5, 3, 3, '2026-08-20', '2026-08-24',
    4, 556.00, 132.00, 0.00, 688.00,
    20.00, 12.50, 83.50, 751.50, 751.50, 0.00,
    'Paid', 'Card', '2026-08-24'
);

-- Active Stay Preliminary Invoice for Reservation 2 (Elena Rostova)
-- 4 nights * $149 = $596 room charges + $93 services = $689 subtotal. Discount: $0. Tax @ 12.5%: $86.13. Grand Total: $775.13. Paid deposit: $300. Balance: $475.13
INSERT INTO invoices (
    invoice_number, reservation_id, guest_id, room_id, check_in_date, check_out_date,
    number_of_nights, room_charges, service_charges, additional_charges, subtotal,
    discount, tax_percentage, tax_amount, grand_total, paid_amount, balance_amount,
    payment_status, payment_method, invoice_date
) VALUES (
    'INV-2026-0002', 2, 2, 4, '2026-08-26', '2026-08-30',
    4, 596.00, 93.00, 0.00, 689.00,
    0.00, 12.50, 86.13, 775.13, 300.00, 475.13,
    'Partially Paid', 'Card', '2026-08-26'
);

-- 12. Payments
INSERT INTO payments (invoice_id, reservation_id, amount, payment_method, payment_status, transaction_reference, payment_date, notes, processed_by_user_id) VALUES
(1, 5, 751.50, 'Card', 'Success', 'TXN-VISA-98214', '2026-08-24 10:50:00', 'Full checkout settlement settled via Visa Card ending 4412', 3),
(2, 2, 300.00, 'Card', 'Success', 'TXN-MC-30491', '2026-08-26 14:20:00', 'Advance reservation deposit paid at check-in', 3);
