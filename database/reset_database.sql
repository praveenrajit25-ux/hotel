-- =====================================================================
-- KR LUXE HOTEL & RESORTS - DATABASE RESET SCRIPT (SQL)
-- Re-initializes clean database with rooms and services, clearing all dummy bookings.
-- Run this script to reset the database to a fresh state.
-- =====================================================================

-- Drop existing tables
DROP TABLE IF EXISTS guest_services;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS guests;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS hotel_settings;

-- 1. HOTEL SETTINGS
CREATE TABLE hotel_settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    hotel_name TEXT NOT NULL DEFAULT 'KR LUXE HOTEL & RESORT',
    tagline TEXT DEFAULT '5-Star Luxury Hospitality & Property Management',
    address TEXT DEFAULT '108 Grand Luxury Boulevard, Suite 500, Chennai, India',
    phone TEXT DEFAULT '+91 98421 00000',
    email TEXT DEFAULT 'reservations@krluxe.com',
    website TEXT DEFAULT 'https://krluxe.hotel',
    currency_symbol TEXT DEFAULT '₹',
    tax_percentage REAL DEFAULT 18.00,
    check_in_time TEXT DEFAULT '14:00',
    check_out_time TEXT DEFAULT '11:00',
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- 2. ROOM INVENTORY
CREATE TABLE rooms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    number TEXT UNIQUE NOT NULL,
    type TEXT NOT NULL,
    floor INTEGER NOT NULL,
    price_per_night REAL NOT NULL,
    capacity INTEGER NOT NULL DEFAULT 2,
    status TEXT NOT NULL DEFAULT 'Available',
    description TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- 3. GUESTS MASTER
CREATE TABLE guests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    gender TEXT DEFAULT 'Other',
    dob TEXT,
    phone TEXT NOT NULL,
    email TEXT NOT NULL,
    address TEXT,
    id_proof_type TEXT DEFAULT 'Aadhaar',
    id_proof_number TEXT,
    nationality TEXT DEFAULT 'Indian',
    registered_date TEXT DEFAULT CURRENT_TIMESTAMP
);

-- 4. RESERVATIONS (BOOKINGS - STARTS CLEAN WITH ZERO DUMMY DATA)
CREATE TABLE reservations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guest_id INTEGER,
    guest_name TEXT NOT NULL,
    guest_phone TEXT,
    guest_email TEXT,
    room_id INTEGER,
    room_number TEXT NOT NULL,
    room_type TEXT NOT NULL,
    check_in_date TEXT NOT NULL,
    check_out_date TEXT NOT NULL,
    number_of_guests INTEGER DEFAULT 1,
    status TEXT NOT NULL DEFAULT 'Confirmed',
    special_requests TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE SET NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- 5. GUEST FOLIO INVOICES
CREATE TABLE invoices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_number TEXT UNIQUE NOT NULL,
    reservation_id INTEGER NOT NULL,
    guest_name TEXT NOT NULL,
    room_number TEXT NOT NULL,
    room_type TEXT NOT NULL,
    check_in_date TEXT NOT NULL,
    check_out_date TEXT NOT NULL,
    number_of_nights INTEGER NOT NULL,
    room_charges REAL NOT NULL,
    service_charges REAL DEFAULT 0.00,
    additional_charges REAL DEFAULT 0.00,
    subtotal REAL NOT NULL,
    discount REAL DEFAULT 0.00,
    tax_percentage REAL DEFAULT 18.00,
    tax_amount REAL NOT NULL,
    grand_total REAL NOT NULL,
    paid_amount REAL DEFAULT 0.00,
    balance_amount REAL DEFAULT 0.00,
    payment_status TEXT NOT NULL DEFAULT 'Pending',
    payment_method TEXT DEFAULT 'Credit_Card',
    invoice_date TEXT NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);

-- 6. HOTEL AMENITIES & SERVICES CATALOG
CREATE TABLE services (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    price REAL NOT NULL,
    description TEXT,
    status TEXT NOT NULL DEFAULT 'Active'
);

-- 7. GUEST SERVICE CHARGES
CREATE TABLE guest_services (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    reservation_id INTEGER NOT NULL,
    guest_name TEXT NOT NULL,
    room_number TEXT NOT NULL,
    service_id INTEGER NOT NULL,
    service_name TEXT NOT NULL,
    quantity INTEGER DEFAULT 1,
    unit_price REAL NOT NULL,
    total_price REAL NOT NULL,
    service_date TEXT DEFAULT CURRENT_TIMESTAMP,
    status TEXT DEFAULT 'Completed',
    notes TEXT,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE RESTRICT
);

-- 8. STAFF DIRECTORY
CREATE TABLE staff (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_code TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    username TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    salary REAL DEFAULT 0.00,
    joining_date TEXT,
    status TEXT DEFAULT 'Active'
);

-- =====================================================================
-- SEED INITIAL DATA (DEFAULT SETTINGS, ROOMS, SERVICES, AND STAFF)
-- =====================================================================

-- Seed Hotel Settings
INSERT INTO hotel_settings (hotel_name, tagline, address, phone, email, currency_symbol, tax_percentage)
VALUES ('KR LUXE HOTEL & RESORT', '5-Star Luxury Hospitality & Property Management', '108 Grand Luxury Boulevard, Suite 500, Chennai, India', '+91 98421 00000', 'reservations@krluxe.com', '₹', 18.00);

-- Seed Hotel Rooms (All Ready & Available for Real Bookings)
INSERT INTO rooms (number, type, floor, price_per_night, capacity, status, description) VALUES
('101', 'Single', 1, 3500.00, 1, 'Available', 'Garden-view single executive bedroom with marble bath'),
('102', 'Double', 1, 5500.00, 2, 'Available', 'Courtyard double bed with plush duvet & city skyline view'),
('201', 'Deluxe', 2, 8500.00, 2, 'Available', 'Spacious deluxe king bed with balcony, rain shower & espresso bar'),
('202', 'Deluxe', 2, 8500.00, 2, 'Available', 'Corner deluxe suite with panoramic garden & poolside view'),
('203', 'Double', 2, 5500.00, 2, 'Available', 'Luxury twin double bed with workstation & high-speed Wi-Fi'),
('204', 'Deluxe', 2, 8500.00, 2, 'Available', 'Ocean-facing premium deluxe room with private sun deck'),
('301', 'Suite', 3, 14000.00, 3, 'Available', 'Executive presidential suite with separate living parlor & jacuzzi'),
('302', 'Suite', 3, 14000.00, 3, 'Available', 'Royal penthouse suite with sunset terrace & butler pantry'),
('303', 'Executive', 3, 18500.00, 4, 'Available', 'Presidential diplomatic suite with private dining, lounge & boardroom');

-- Seed Services Catalog
INSERT INTO services (name, category, price, description, status) VALUES
('Gourmet In-Suite Breakfast', 'Dining', 1200.00, 'Continental & South Indian breakfast spread with freshly squeezed juice and artisan breads', 'Active'),
('Ayurvedic Spa & Herbal Massage', 'Wellness', 3500.00, '60-minute relaxing organic herbal oil therapy by licensed masseurs', 'Active'),
('Airport Luxury Chauffeur Transfer', 'Transport', 2200.00, 'Private Mercedes E-Class transfer to/from Chennai International Airport', 'Active'),
('Evening Fine-Dining Tasting Menu', 'Dining', 4500.00, '5-course chef-curated royal tasting menu at The Jewel of KR dining room', 'Active'),
('Express Garment Pressing & Laundry', 'Housekeeping', 850.00, 'Same-day laundry, steam press, and luxury garment packaging', 'Active'),
('Sunset Champagne & Canapes Basket', 'Amenities', 2800.00, 'Chilled Moët & Chandon bottle with imported cheeses and fresh berries', 'Active');

-- Seed Staff Directory (Including Manager Yugan G & Administrator Suthaker)
INSERT INTO staff (employee_code, full_name, username, role, phone, email, salary, joining_date, status) VALUES
('EMP-001', 'Suthaker', 'suthaker', 'Admin', '+91 98765 43210', 'suthaker09s@gmail.com', 95000.00, '2024-01-01', 'Active'),
('EMP-002', 'Yugan G', 'yugan', 'Manager', '+91 98421 00101', 'yugan0302@gmail.com', 75000.00, '2024-03-01', 'Active'),
('EMP-003', 'Priya Sundaram', 'priya.s', 'Receptionist', '+91 98421 00102', 'priya.reception@krluxe.com', 42000.00, '2024-04-15', 'Active'),
('EMP-004', 'Chef Antoine Laurent', 'chef.antoine', 'Chef', '+91 98421 00103', 'antoine.kitchen@krluxe.com', 65000.00, '2024-02-10', 'Active');

-- 3. GUESTS MASTER SEED
INSERT INTO guests (id, full_name, gender, dob, phone, email, address, id_proof_type, id_proof_number, nationality) VALUES
(1, 'Alexander Vance', 'Male', '1985-04-12', '+91 98421 00142', 'a.vance@example.com', '124 Beacon St, Boston, MA', 'Passport', 'P9842104', 'United States'),
(2, 'Elena Rostova', 'Female', '1992-09-23', '+91 98421 00188', 'elena.rostova@example.com', '450 Park Ave, New York, NY', 'Driver_License', 'DL-NY-84920', 'United States'),
(3, 'Marcus Sterling', 'Male', '1978-11-30', '+91 98421 00277', 'm.sterling@capital.io', '88 Montgomery St, San Francisco, CA', 'Passport', 'P3301984', 'United Kingdom'),
(4, 'Sophia Chen', 'Female', '1995-02-18', '+91 98421 00391', 'sophia.chen@tech.org', '720 Pine St, Seattle, WA', 'National_Id', 'NID-99201', 'Canada'),
(5, 'David Miller', 'Male', '1989-07-04', '+91 98421 00419', 'dmiller@innovate.com', '310 Michigan Ave, Chicago, IL', 'Passport', 'P7712044', 'United States');

-- 4. RESERVATIONS SEED (Active, Confirmed, and Checked-Out)
INSERT INTO reservations (id, guest_id, guest_name, guest_phone, guest_email, room_id, room_number, room_type, check_in_date, check_out_date, number_of_guests, status, special_requests) VALUES
(1001, 1, 'Alexander Vance', '+91 98421 00142', 'a.vance@example.com', 2, '102', 'Double', '2026-10-10', '2026-10-15', 2, 'Checked_In', 'High floor, late checkout requested'),
(1002, 2, 'Elena Rostova', '+91 98421 00188', 'elena.rostova@example.com', 4, '202', 'Deluxe', '2026-10-11', '2026-10-16', 2, 'Checked_In', 'Champagne on arrival & extra down pillows'),
(1003, 3, 'Marcus Sterling', '+91 98421 00277', 'm.sterling@capital.io', 7, '301', 'Suite', '2026-10-09', '2026-10-14', 3, 'Checked_In', 'Airport Mercedes transfer required'),
(1004, 4, 'Sophia Chen', '+91 98421 00391', 'sophia.chen@tech.org', 3, '201', 'Deluxe', '2026-10-14', '2026-10-18', 2, 'Confirmed', 'Quiet room away from elevators'),
(1005, 5, 'David Miller', '+91 98421 00419', 'dmiller@innovate.com', 8, '302', 'Suite', '2026-10-05', '2026-10-09', 4, 'Checked_Out', 'VIP Executive lounge access');

-- Update room occupancy based on active stays
UPDATE rooms SET status = 'Occupied' WHERE number IN ('102', '202', '301');
UPDATE rooms SET status = 'Reserved' WHERE number = '201';

-- 5. INVOICES SEED (Matching Folios)
INSERT INTO invoices (id, invoice_number, reservation_id, guest_name, room_number, room_type, check_in_date, check_out_date, number_of_nights, room_charges, service_charges, subtotal, discount, tax_amount, grand_total, paid_amount, balance_amount, payment_status, payment_method, invoice_date) VALUES
(1, 'INV-2026-1001', 1001, 'Alexander Vance', '102', 'Double', '2026-10-10', '2026-10-15', 5, 27500.00, 2400.00, 29900.00, 0.00, 5382.00, 35282.00, 35282.00, 0.00, 'Paid', 'Credit_Card', '2026-10-10'),
(2, 'INV-2026-1002', 1002, 'Elena Rostova', '202', 'Deluxe', '2026-10-11', '2026-10-16', 5, 42500.00, 3500.00, 46000.00, 2000.00, 7920.00, 51920.00, 25000.00, 26920.00, 'Partially_Paid', 'Credit_Card', '2026-10-11'),
(3, 'INV-2026-1003', 1003, 'Marcus Sterling', '301', 'Suite', '2026-10-09', '2026-10-14', 5, 70000.00, 5000.00, 75000.00, 5000.00, 12600.00, 82600.00, 82600.00, 0.00, 'Paid', 'UPI', '2026-10-09'),
(4, 'INV-2026-1004', 1004, 'Sophia Chen', '201', 'Deluxe', '2026-10-14', '2026-10-18', 4, 34000.00, 0.00, 34000.00, 0.00, 6120.00, 40120.00, 0.00, 40120.00, 'Pending', 'Credit_Card', '2026-10-13'),
(5, 'INV-2026-1005', 1005, 'David Miller', '302', 'Suite', '2026-10-05', '2026-10-09', 4, 56000.00, 5350.00, 61350.00, 0.00, 11043.00, 72393.00, 72393.00, 0.00, 'Paid', 'Credit_Card', '2026-10-05');

-- 6. GUEST SERVICES SEED (Orders placed by in-house guests)
INSERT INTO guest_services (id, reservation_id, guest_name, room_number, service_id, service_name, quantity, unit_price, total_price, status, notes) VALUES
(1, 1001, 'Alexander Vance', '102', 1, 'Gourmet In-Suite Breakfast', 2, 1200.00, 2400.00, 'Completed', 'Continental breakfast delivered to Room 102'),
(2, 1002, 'Elena Rostova', '202', 2, 'Ayurvedic Spa & Herbal Massage', 1, 3500.00, 3500.00, 'Completed', 'Aromatherapy session at Royal Spa'),
(3, 1003, 'Marcus Sterling', '301', 3, 'Airport Luxury Chauffeur Transfer', 1, 2200.00, 2200.00, 'Completed', 'Mercedes E-Class arrival transfer'),
(4, 1003, 'Marcus Sterling', '301', 6, 'Sunset Champagne & Canapes Basket', 1, 2800.00, 2800.00, 'Completed', 'Delivered to Suite 301');


