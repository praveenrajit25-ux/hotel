import fs from 'fs';
import path from 'path';
import initSqlJs, { Database } from 'sql.js';

const DB_DIR = path.resolve(process.cwd(), 'database');
const DB_FILE = path.join(DB_DIR, 'hotel_management.db');
const RESET_SQL_FILE = path.join(DB_DIR, 'reset_database.sql');
const SCHEMA_SQL_FILE = path.join(DB_DIR, 'schema.sql');

let dbInstance: Database | null = null;

// Ensure database directory exists
if (!fs.existsSync(DB_DIR)) {
  fs.mkdirSync(DB_DIR, { recursive: true });
}

/**
 * Saves in-memory database to disk file (hotel_management.db)
 */
export function saveDbToDisk(): void {
  if (!dbInstance) return;
  try {
    const data = dbInstance.export();
    fs.writeFileSync(DB_FILE, Buffer.from(data));
  } catch (err) {
    console.error('Failed to save SQLite database to disk:', err);
  }
}

/**
 * Initializes SQLite database via SQL.js
 */
export async function getDatabase(): Promise<Database> {
  if (dbInstance) return dbInstance;

  const SQL = await initSqlJs();

  if (fs.existsSync(DB_FILE)) {
    try {
      const fileBuffer = fs.readFileSync(DB_FILE);
      dbInstance = new SQL.Database(fileBuffer);
      return dbInstance;
    } catch (err) {
      console.warn('Could not read existing DB file, creating fresh one:', err);
    }
  }

  // Create new database
  dbInstance = new SQL.Database();

  // Execute reset script to create tables and seed default rooms and services
  if (fs.existsSync(RESET_SQL_FILE)) {
    const initSql = fs.readFileSync(RESET_SQL_FILE, 'utf-8');
    dbInstance.run(initSql);
  } else if (fs.existsSync(SCHEMA_SQL_FILE)) {
    const schemaSql = fs.readFileSync(SCHEMA_SQL_FILE, 'utf-8');
    dbInstance.run(schemaSql);
  }

  saveDbToDisk();
  return dbInstance;
}

/**
 * Resets database from the reset_database.sql file
 */
export async function resetDatabase(): Promise<{ success: boolean; message: string }> {
  const SQL = await initSqlJs();
  const newDb = new SQL.Database();
  const resetSql = fs.readFileSync(RESET_SQL_FILE, 'utf-8');
  newDb.run(resetSql);
  dbInstance = newDb;
  saveDbToDisk();
  return { success: true, message: 'Database reset successfully with clean schema and room inventory!' };
}

// =====================================================================
// QUERY HELPERS
// =====================================================================

function queryAll<T = any>(db: Database, sql: string, params: any[] = []): T[] {
  const stmt = db.prepare(sql);
  stmt.bind(params);
  const rows: T[] = [];
  while (stmt.step()) {
    rows.push(stmt.getAsObject() as unknown as T);
  }
  stmt.free();
  return rows;
}

function queryOne<T = any>(db: Database, sql: string, params: any[] = []): T | null {
  const rows = queryAll<T>(db, sql, params);
  return rows.length > 0 ? rows[0] : null;
}

// =====================================================================
// DATA ACCESS FUNCTIONS
// =====================================================================

export async function getDbStats() {
  const db = await getDatabase();
  const tables = ['rooms', 'reservations', 'invoices', 'services', 'guest_services', 'guests', 'staff'];
  const counts: Record<string, number> = {};

  for (const table of tables) {
    try {
      const res = queryOne<{ count: number }>(db, `SELECT COUNT(*) as count FROM ${table}`);
      counts[table] = res?.count ?? 0;
    } catch {
      counts[table] = 0;
    }
  }

  return {
    databaseFile: 'database/hotel_management.db',
    status: 'Connected & Active',
    tables: counts,
    fileSizeBytes: fs.existsSync(DB_FILE) ? fs.statSync(DB_FILE).size : 0
  };
}

export async function getAllRooms() {
  const db = await getDatabase();
  const rows = queryAll(db, 'SELECT * FROM rooms ORDER BY number ASC');
  return rows.map(r => ({
    id: r.id,
    number: r.number,
    type: r.type,
    floor: r.floor,
    pricePerNight: r.price_per_night,
    capacity: r.capacity,
    status: r.status,
    description: r.description
  }));
}

export async function addRoom(room: any) {
  const db = await getDatabase();
  db.run(
    'INSERT INTO rooms (number, type, floor, price_per_night, capacity, status, description) VALUES (?, ?, ?, ?, ?, ?, ?)',
    [room.number, room.type, room.floor, room.pricePerNight, room.capacity, room.status || 'Available', room.description || '']
  );
  saveDbToDisk();
  return queryOne(db, 'SELECT * FROM rooms WHERE number = ?', [room.number]);
}

export async function getAllReservations() {
  const db = await getDatabase();
  const rows = queryAll(db, 'SELECT * FROM reservations ORDER BY id DESC');
  return rows.map(r => ({
    id: r.id,
    guestId: r.guest_id || 1,
    guestName: r.guest_name,
    guestPhone: r.guest_phone || '',
    guestEmail: r.guest_email || '',
    roomId: r.room_id || 1,
    roomNumber: r.room_number,
    roomType: r.room_type,
    checkInDate: r.check_in_date,
    checkOutDate: r.check_out_date,
    numberOfGuests: r.number_of_guests,
    status: r.status,
    specialRequests: r.special_requests,
    createdAt: r.created_at
  }));
}

export async function createReservationAndInvoice(data: {
  guestName: string;
  guestEmail: string;
  guestPhone: string;
  roomId?: number;
  roomNumber: string;
  roomType: string;
  checkInDate: string;
  checkOutDate: string;
  numberOfGuests?: number;
  numberOfNights: number;
  roomCharges: number;
  discount?: number;
  taxAmount: number;
  grandTotal: number;
  specialRequests?: string;
  paymentMethod?: string;
  payNow?: boolean;
}) {
  const db = await getDatabase();

  // 1. Insert Guest if not exists
  let guest = queryOne(db, 'SELECT id FROM guests WHERE email = ?', [data.guestEmail]);
  let guestId: number;
  if (!guest) {
    db.run(
      'INSERT INTO guests (full_name, phone, email, nationality) VALUES (?, ?, ?, ?)',
      [data.guestName, data.guestPhone || '+91 98421 00000', data.guestEmail, 'Indian']
    );
    const lastGuest = queryOne(db, 'SELECT last_insert_rowid() as id');
    guestId = lastGuest.id;
  } else {
    guestId = guest.id;
  }

  // 2. Insert Reservation into SQL table
  db.run(
    `INSERT INTO reservations 
    (guest_id, guest_name, guest_phone, guest_email, room_number, room_type, check_in_date, check_out_date, number_of_guests, status, special_requests)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Confirmed', ?)`,
    [
      guestId,
      data.guestName,
      data.guestPhone || '',
      data.guestEmail,
      data.roomNumber,
      data.roomType,
      data.checkInDate,
      data.checkOutDate,
      data.numberOfGuests || 1,
      data.specialRequests || ''
    ]
  );

  const resRow = queryOne(db, 'SELECT last_insert_rowid() as id');
  const reservationId = resRow.id;

  // 3. Generate Invoice Number & Insert Invoice into SQL table
  const invoiceNumber = `INV-${new Date().getFullYear()}-${String(reservationId).padStart(4, '0')}`;
  const paidAmount = data.payNow ? data.grandTotal : 0;
  const balanceAmount = data.payNow ? 0 : data.grandTotal;
  const subtotal = data.roomCharges;
  const paymentStatus = data.payNow ? 'Paid' : 'Pending';

  db.run(
    `INSERT INTO invoices 
    (invoice_number, reservation_id, guest_name, room_number, room_type, check_in_date, check_out_date, number_of_nights, room_charges, service_charges, subtotal, discount, tax_amount, grand_total, paid_amount, balance_amount, payment_status, payment_method, invoice_date)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0.00, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
    [
      invoiceNumber,
      reservationId,
      data.guestName,
      data.roomNumber,
      data.roomType,
      data.checkInDate,
      data.checkOutDate,
      data.numberOfNights,
      data.roomCharges,
      subtotal,
      data.discount || 0,
      data.taxAmount,
      data.grandTotal,
      paidAmount,
      balanceAmount,
      paymentStatus,
      data.paymentMethod || 'Credit_Card',
      new Date().toISOString().split('T')[0]
    ]
  );

  // 4. Update Room status to 'Reserved' in SQL
  db.run('UPDATE rooms SET status = ? WHERE number = ?', ['Reserved', data.roomNumber]);

  // 5. Persist to file
  saveDbToDisk();

  const newReservation = queryOne(db, 'SELECT * FROM reservations WHERE id = ?', [reservationId]);
  const newInvoice = queryOne(db, 'SELECT * FROM invoices WHERE reservation_id = ?', [reservationId]);

  return {
    reservation: {
      id: newReservation.id,
      guestId: newReservation.guest_id,
      guestName: newReservation.guest_name,
      guestPhone: newReservation.guest_phone,
      guestEmail: newReservation.guest_email,
      roomId: 1,
      roomNumber: newReservation.room_number,
      roomType: newReservation.room_type,
      checkInDate: newReservation.check_in_date,
      checkOutDate: newReservation.check_out_date,
      numberOfGuests: newReservation.number_of_guests,
      status: newReservation.status,
      specialRequests: newReservation.special_requests,
      createdAt: newReservation.created_at
    },
    invoice: {
      id: newInvoice.id,
      invoiceNumber: newInvoice.invoice_number,
      reservationId: newInvoice.reservation_id,
      guestName: newInvoice.guest_name,
      roomNumber: newInvoice.room_number,
      roomType: newInvoice.room_type,
      checkInDate: newInvoice.check_in_date,
      checkOutDate: newInvoice.check_out_date,
      numberOfNights: newInvoice.number_of_nights,
      roomCharges: newInvoice.room_charges,
      serviceCharges: newInvoice.service_charges,
      additionalCharges: 0,
      subtotal: newInvoice.room_charges + newInvoice.service_charges,
      discount: newInvoice.discount,
      taxPercentage: 18,
      taxAmount: newInvoice.tax_amount,
      grandTotal: newInvoice.grand_total,
      paidAmount: newInvoice.paid_amount,
      balanceAmount: newInvoice.balance_amount,
      paymentStatus: newInvoice.payment_status,
      paymentMethod: newInvoice.payment_method,
      invoiceDate: newInvoice.invoice_date,
      payments: []
    }
  };
}

export async function updateReservationStatus(id: number, status: string) {
  const db = await getDatabase();
  db.run('UPDATE reservations SET status = ? WHERE id = ?', [status, id]);
  saveDbToDisk();
  return queryOne(db, 'SELECT * FROM reservations WHERE id = ?', [id]);
}

export async function getAllInvoices() {
  const db = await getDatabase();
  const rows = queryAll(db, 'SELECT * FROM invoices ORDER BY id DESC');
  return rows.map(r => ({
    id: r.id,
    invoiceNumber: r.invoice_number,
    reservationId: r.reservation_id,
    guestName: r.guest_name,
    roomNumber: r.room_number,
    roomType: r.room_type,
    checkInDate: r.check_in_date,
    checkOutDate: r.check_out_date,
    numberOfNights: r.number_of_nights,
    roomCharges: r.room_charges,
    serviceCharges: r.service_charges,
    additionalCharges: 0,
    subtotal: r.room_charges + r.service_charges,
    discount: r.discount,
    taxPercentage: 18,
    taxAmount: r.tax_amount,
    grandTotal: r.grand_total,
    paidAmount: r.paid_amount,
    balanceAmount: r.balance_amount,
    paymentStatus: r.payment_status,
    paymentMethod: r.payment_method,
    invoiceDate: r.invoice_date,
    payments: []
  }));
}

export async function settleInvoicePayment(id: number) {
  const db = await getDatabase();
  const invoice = queryOne(db, 'SELECT * FROM invoices WHERE id = ?', [id]);
  if (!invoice) throw new Error('Invoice not found');

  db.run(
    'UPDATE invoices SET paid_amount = grand_total, balance_amount = 0.00, payment_status = ? WHERE id = ?',
    ['Paid', id]
  );
  saveDbToDisk();
  return queryOne(db, 'SELECT * FROM invoices WHERE id = ?', [id]);
}

export async function getAllServices() {
  const db = await getDatabase();
  return queryAll(db, 'SELECT * FROM services WHERE status = "Active" ORDER BY id ASC');
}

export async function getAllGuestServices() {
  const db = await getDatabase();
  const rows = queryAll(db, 'SELECT * FROM guest_services ORDER BY id DESC');
  return rows.map(r => ({
    id: r.id,
    reservationId: r.reservation_id,
    guestName: r.guest_name,
    roomNumber: r.room_number,
    serviceId: r.service_id,
    serviceName: r.service_name,
    quantity: r.quantity,
    unitPrice: r.unit_price,
    totalPrice: r.total_price,
    serviceDate: r.service_date,
    status: r.status,
    notes: r.notes
  }));
}

export async function addGuestService(data: {
  reservationId: number;
  guestName: string;
  roomNumber: string;
  serviceId: number;
  serviceName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  notes?: string;
}) {
  const db = await getDatabase();
  db.run(
    `INSERT INTO guest_services 
    (reservation_id, guest_name, room_number, service_id, service_name, quantity, unit_price, total_price, status, notes)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Preparing', ?)`,
    [
      data.reservationId,
      data.guestName,
      data.roomNumber,
      data.serviceId,
      data.serviceName,
      data.quantity,
      data.unitPrice,
      data.totalPrice,
      data.notes || ''
    ]
  );

  // Update invoice service charges
  db.run(
    `UPDATE invoices 
     SET service_charges = service_charges + ?,
         subtotal = subtotal + ?,
         grand_total = grand_total + ?,
         balance_amount = balance_amount + ?
     WHERE reservation_id = ?`,
    [data.totalPrice, data.totalPrice, data.totalPrice, data.totalPrice, data.reservationId]
  );

  saveDbToDisk();
  const row = queryOne(db, 'SELECT last_insert_rowid() as id');
  return queryOne(db, 'SELECT * FROM guest_services WHERE id = ?', [row.id]);
}

export async function updateGuestServiceStatus(id: number, status: string) {
  const db = await getDatabase();
  db.run('UPDATE guest_services SET status = ? WHERE id = ?', [status, id]);
  saveDbToDisk();
  return queryOne(db, 'SELECT * FROM guest_services WHERE id = ?', [id]);
}

export async function getAllStaff() {
  const db = await getDatabase();
  return queryAll(db, 'SELECT * FROM staff ORDER BY id ASC');
}

export async function getHotelSettings() {
  const db = await getDatabase();
  const row = queryOne(db, 'SELECT * FROM hotel_settings LIMIT 1');
  if (!row) {
    return {
      hotelName: 'KR LUXE HOTEL & RESORT',
      tagline: '5-Star Luxury Hospitality & Property Management',
      address: '108 Grand Luxury Boulevard, Suite 500, Chennai, India',
      phone: '+91 98421 00000',
      email: 'reservations@krluxe.com',
      website: 'https://krluxe.hotel',
      currencySymbol: '₹',
      taxPercentage: 18,
      checkInTime: '14:00',
      checkOutTime: '11:00'
    };
  }
  return {
    hotelName: row.hotel_name,
    tagline: row.tagline,
    address: row.address,
    phone: row.phone,
    email: row.email,
    website: row.website,
    currencySymbol: row.currency_symbol || '₹',
    taxPercentage: row.tax_percentage || 18,
    checkInTime: row.check_in_time || '14:00',
    checkOutTime: row.check_out_time || '11:00'
  };
}

/**
 * Generates a full SQL dump script containing all CREATE TABLE and INSERT INTO statements.
 */
export async function exportSqlDump(): Promise<string> {
  const db = await getDatabase();
  let dump = `-- =====================================================================\n`;
  dump += `-- KR LUXE HOTEL - LIVE SQL DATABASE EXPORT DUMP\n`;
  dump += `-- Generated: ${new Date().toISOString()}\n`;
  dump += `-- =====================================================================\n\n`;

  const tables = ['hotel_settings', 'rooms', 'guests', 'reservations', 'invoices', 'services', 'guest_services', 'staff'];

  for (const table of tables) {
    const rows = queryAll(db, `SELECT * FROM ${table}`);
    dump += `-- Table: ${table} (${rows.length} records)\n`;
    for (const row of rows) {
      const keys = Object.keys(row);
      const cols = keys.join(', ');
      const vals = keys.map(k => {
        const val = row[k];
        if (val === null || val === undefined) return 'NULL';
        if (typeof val === 'number') return val;
        return `'${String(val).replace(/'/g, "''")}'`;
      }).join(', ');
      dump += `INSERT INTO ${table} (${cols}) VALUES (${vals});\n`;
    }
    dump += `\n`;
  }

  return dump;
}
