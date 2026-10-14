import fs from 'fs';
import path from 'path';
import initSqlJs from 'sql.js';

/**
 * =========================================================================
 * KR LUXE HOTEL & RESORTS - SQL DATABASE RESET TOOL
 * =========================================================================
 * Run this script anytime via:
 *   npm run db:reset
 *   or tsx server/reset_db.ts
 *
 * It reads database/reset_database.sql and builds a clean hotel_management.db
 * file with fresh room inventory, services, staff, and zero dummy bookings.
 * =========================================================================
 */

async function main() {
  const dbDir = path.resolve(process.cwd(), 'database');
  const dbFile = path.join(dbDir, 'hotel_management.db');
  const resetSqlFile = path.join(dbDir, 'reset_database.sql');

  console.log('🔄 Initializing SQL Database Reset...');
  console.log(`📁 Reset script: ${resetSqlFile}`);
  console.log(`📁 Target database: ${dbFile}`);

  if (!fs.existsSync(resetSqlFile)) {
    console.error(`❌ Error: Could not find ${resetSqlFile}`);
    process.exit(1);
  }

  const sqlContent = fs.readFileSync(resetSqlFile, 'utf-8');

  try {
    const SQL = await initSqlJs();
    const db = new SQL.Database();

    // Execute the SQL reset script
    db.run(sqlContent);

    // Export binary database to file
    const data = db.export();
    fs.writeFileSync(dbFile, Buffer.from(data));

    // Verify tables and counts
    const tablesRes = db.exec("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;");
    const tableNames = tablesRes[0]?.values.map(v => v[0]) || [];

    console.log('✅ Database reset successfully!');
    console.log(`📊 Active SQL Tables (${tableNames.length}):`, tableNames.join(', '));

    const roomCount = db.exec('SELECT count(*) FROM rooms')[0]?.values[0][0];
    const serviceCount = db.exec('SELECT count(*) FROM services')[0]?.values[0][0];
    const staffCount = db.exec('SELECT count(*) FROM staff')[0]?.values[0][0];
    const resCount = db.exec('SELECT count(*) FROM reservations')[0]?.values[0][0];

    console.log(`🏨 Ready Rooms (All Available): ${roomCount}`);
    console.log(`🛎️ Services Catalog: ${serviceCount}`);
    console.log(`👥 Staff Members: ${staffCount}`);
    console.log(`📅 Active Bookings: ${resCount} (Clean state, ready for your real bookings!)`);
    console.log(`💾 Saved to: database/hotel_management.db (${fs.statSync(dbFile).size} bytes)`);
    console.log('✨ You can now run `npm run dev` and start booking rooms!');
  } catch (err) {
    console.error('❌ Failed to reset database:', err);
    process.exit(1);
  }
}

main();
