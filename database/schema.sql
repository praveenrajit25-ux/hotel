-- =====================================================================
-- KR LUXE HOTEL & RESORTS - RELATIONAL DATABASE SCHEMA (SQL)
-- Standard ANSI / SQLite / MySQL Compatible DDL
-- Database File: database/hotel_management.db
-- =====================================================================

-- 1. HOTEL SETTINGS
CREATE TABLE IF NOT EXISTS hotel_settings (
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
CREATE TABLE IF NOT EXISTS rooms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    number TEXT UNIQUE NOT NULL,
    type TEXT NOT NULL, -- 'Deluxe' | 'Suite' | 'Executive' | 'Single' | 'Double'
    floor INTEGER NOT NULL,
    price_per_night REAL NOT NULL,
    capacity INTEGER NOT NULL DEFAULT 2,
    status TEXT NOT NULL DEFAULT 'Available', -- 'Available' | 'Reserved' | 'Occupied' | 'Cleaning' | 'Maintenance'
    description TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- 3. GUESTS MASTER
CREATE TABLE IF NOT EXISTS guests (
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

-- 4. RESERVATIONS (BOOKINGS)
CREATE TABLE IF NOT EXISTS reservations (
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
    status TEXT NOT NULL DEFAULT 'Confirmed', -- 'Confirmed' | 'Checked_In' | 'Checked_Out' | 'Cancelled'
    special_requests TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE SET NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- 5. GUEST FOLIO INVOICES
CREATE TABLE IF NOT EXISTS invoices (
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
    payment_status TEXT NOT NULL DEFAULT 'Pending', -- 'Pending' | 'Partially_Paid' | 'Paid'
    payment_method TEXT DEFAULT 'Credit_Card',
    invoice_date TEXT NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);

-- 6. HOTEL AMENITIES & SERVICES CATALOG
CREATE TABLE IF NOT EXISTS services (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    price REAL NOT NULL,
    description TEXT,
    status TEXT NOT NULL DEFAULT 'Active'
);

-- 7. GUEST SERVICE CHARGES (In-room dining & requests)
CREATE TABLE IF NOT EXISTS guest_services (
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
    status TEXT DEFAULT 'Completed', -- 'Preparing' | 'In_Transit' | 'Completed'
    notes TEXT,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE RESTRICT
);

-- 8. STAFF DIRECTORY & ROLES
CREATE TABLE IF NOT EXISTS staff (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_code TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    username TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL, -- 'Admin' | 'Manager' | 'Receptionist' | 'Housekeeping' | 'Chef' | 'Staff'
    phone TEXT,
    email TEXT,
    salary REAL DEFAULT 0.00,
    joining_date TEXT,
    status TEXT DEFAULT 'Active'
);

-- INDEXES FOR MAXIMUM QUERY PERFORMANCE
CREATE INDEX IF NOT EXISTS idx_rooms_number ON rooms(number);
CREATE INDEX IF NOT EXISTS idx_rooms_status ON rooms(status);
CREATE INDEX IF NOT EXISTS idx_reservations_room ON reservations(room_number);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);
CREATE INDEX IF NOT EXISTS idx_reservations_dates ON reservations(check_in_date, check_out_date);
CREATE INDEX IF NOT EXISTS idx_invoices_number ON invoices(invoice_number);
CREATE INDEX IF NOT EXISTS idx_invoices_res ON invoices(reservation_id);
