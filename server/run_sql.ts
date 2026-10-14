import fs from 'fs';
import path from 'path';
import initSqlJs from 'sql.js';

/**
 * =========================================================================
 * KR LUXE HOTEL & RESORTS - SQL RUNNER TOOL
 * =========================================================================
 * Run arbitrary SQL queries or .sql script files on hotel_management.db:
 *
 * Examples:
 *   npm run sql
 *   npm run sql "SELECT * FROM rooms;"
 *   npm run sql "SELECT * FROM reservations;"
 *   npm run sql database/reset_database.sql
 * =========================================================================
 */

function printTable(title: string, columns: string[], rows: any[][]) {
  console.log(`\n📋 TABLE: ${title}`);
  console.log('='.repeat(75));
  console.log(columns.map(c => c.toUpperCase()).join(' | '));
  console.log('-'.repeat(75));
  if (rows.length === 0) {
    console.log('  (No rows found)');
  } else {
    for (const r of rows) {
      console.log(r.map(v => v === null ? 'NULL' : String(v)).join(' | '));
    }
  }
  console.log('-'.repeat(75));
  console.log(`Total: ${rows.length} row(s)\n`);
}

async function main() {
  const dbDir = path.resolve(process.cwd(), 'database');
  const dbFile = path.join(dbDir, 'hotel_management.db');

  const args = process.argv.slice(2);
  const inputArg = args[0];

  const SQL = await initSqlJs();
  let db;

  if (fs.existsSync(dbFile)) {
    const fileBuffer = fs.readFileSync(dbFile);
    db = new SQL.Database(fileBuffer);
  } else {
    db = new SQL.Database();
  }

  // If no argument passed, show live table data previews
  if (!inputArg) {
    console.log('===========================================================================');
    console.log('📊 KR LUXE HOTEL & RESORTS - LIVE SQL DATABASE BROWSER');
    console.log(`📁 File: ${dbFile}`);
    console.log('===========================================================================');

    // 1. Reservations Preview
    try {
      const resRes = db.exec("SELECT id, guest_name, room_number, room_type, check_in_date, check_out_date, status FROM reservations ORDER BY id ASC");
      if (resRes.length > 0) {
        printTable('reservations (Hotel Bookings)', resRes[0].columns, resRes[0].values);
      }
    } catch {}

    // 2. Rooms Preview
    try {
      const roomRes = db.exec("SELECT number, type, floor, price_per_night, status FROM rooms ORDER BY number ASC LIMIT 6");
      if (roomRes.length > 0) {
        printTable('rooms (Inventory Preview - First 6)', roomRes[0].columns, roomRes[0].values);
      }
    } catch {}

    // 3. Invoices Preview
    try {
      const invRes = db.exec("SELECT invoice_number, guest_name, room_number, grand_total, paid_amount, balance_amount, payment_status FROM invoices ORDER BY id ASC");
      if (invRes.length > 0) {
        printTable('invoices (Guest Billing Folios)', invRes[0].columns, invRes[0].values);
      }
    } catch {}

    // 4. Staff Preview
    try {
      const staffRes = db.exec("SELECT employee_code, full_name, role, phone, email, status FROM staff ORDER BY id ASC");
      if (staffRes.length > 0) {
        printTable('staff (Hotel Staff Directory)', staffRes[0].columns, staffRes[0].values);
      }
    } catch {}

    console.log('---------------------------------------------------------------------------');
    console.log('💡 TIP: You can run any custom query anytime:');
    console.log('   npm run sql "SELECT * FROM rooms;"');
    console.log('   npm run sql "SELECT id, guest_name, room_number, status FROM reservations;"');
    console.log('===========================================================================\n');
    return;
  }

  // Check if input is a file path
  let sqlToRun = inputArg;
  if (fs.existsSync(inputArg)) {
    console.log(`📄 Executing SQL script file: ${inputArg}`);
    sqlToRun = fs.readFileSync(inputArg, 'utf-8');
  } else {
    console.log(`⚡ Executing SQL Query: ${sqlToRun}`);
  }

  try {
    const results = db.exec(sqlToRun);

    if (results.length === 0) {
      console.log('✅ Query executed successfully (No rows returned).');
    } else {
      for (const res of results) {
        printTable('QUERY RESULTS', res.columns, res.values);
      }
    }

    // Save changes back to disk
    const data = db.export();
    fs.writeFileSync(dbFile, Buffer.from(data));
  } catch (err: any) {
    console.error('❌ SQL Execution Error:', err.message);
    process.exit(1);
  }
}

main();
