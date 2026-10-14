import fs from 'fs';
import path from 'path';
import readline from 'readline';
import mysql from 'mysql2/promise';

/**
 * =========================================================================
 * KR LUXE HOTEL - MYSQL RUNNER & TABLE VIEWER (DIRECT TCP VIA MYSQL2)
 * =========================================================================
 * Connects directly to MySQL over TCP (localhost:3306).
 * DOES NOT require mysql.exe or MySQL CLI in your Windows PATH!
 *
 * Runs HotelManagementSystem/database/hotel_management.sql, creates all
 * tables, and displays the tables and counts cleanly in your terminal.
 * =========================================================================
 */

async function promptPassword(): Promise<string> {
  const defaultPass = 'Vigensh1@.';

  // Command-line argument
  const arg = process.argv.slice(2).find(a => !a.startsWith('--'));
  if (arg !== undefined) return arg;

  // Environment variable
  if (process.env.MYSQL_PASSWORD !== undefined) return process.env.MYSQL_PASSWORD;

  // If interactive terminal
  if (process.stdin.isTTY) {
    const rl = readline.createInterface({
      input: process.stdin,
      output: process.stdout
    });
    return new Promise(resolve => {
      rl.question(`🔑 Enter MySQL root password (press Enter for '${defaultPass}'): `, answer => {
        rl.close();
        const trimmed = answer.trim();
        resolve(trimmed.length > 0 ? trimmed : defaultPass);
      });
    });
  }

  return defaultPass;
}

async function main() {
  const sqlFile = path.resolve(process.cwd(), 'HotelManagementSystem', 'database', 'hotel_management.sql');

  console.log('============================================================');
  console.log('🐬 KR LUXE HOTEL - DIRECT MYSQL DATABASE RUNNER');
  console.log('   (Pure Node.js TCP - No mysql.exe needed in PATH)');
  console.log('============================================================');

  if (!fs.existsSync(sqlFile)) {
    console.error(`❌ SQL file not found: ${sqlFile}`);
    process.exit(1);
  }

  const password = await promptPassword();
  const host = process.env.MYSQL_HOST || 'localhost';
  const port = Number(process.env.MYSQL_PORT) || 3306;
  const user = process.env.MYSQL_USER || 'root';

  console.log(`\n⏳ Connecting to MySQL server at ${host}:${port} as '${user}'...`);

  let connection;
  try {
    connection = await mysql.createConnection({
      host,
      port,
      user,
      password,
      multipleStatements: true
    });
    console.log('✅ Connected to MySQL successfully!');
  } catch (err: any) {
    console.error('\n❌ Could not connect to MySQL server:');
    console.error(`   Error: ${err.message}`);
    console.log('\n💡 Please check:');
    console.log('   1. Is MySQL Server running? (Start MySQL in XAMPP or Windows Services)');
    console.log('   2. Is the password correct? You entered: ' + (password ? '******' : '(blank)'));
    console.log('\n✨ Fallback: You can use the built-in SQLite database anytime with:');
    console.log('   npm run db:reset');
    console.log('   npm run dev\n');
    process.exit(1);
  }

  try {
    console.log(`📄 Reading ${sqlFile}...`);
    const sqlContent = fs.readFileSync(sqlFile, 'utf-8');

    console.log('⚡ Executing SQL script into MySQL (Creating database `hotel_management` and tables)...');
    await connection.query(sqlContent);
    console.log('✅ All SQL commands executed successfully!');

    // Switch to hotel_management
    await connection.query('USE hotel_management');

    // Show Tables
    console.log('\n============================================================');
    console.log('📊 TABLES IN DATABASE `hotel_management`:');
    console.log('============================================================');
    const [tableRows] = await connection.query('SHOW TABLES');
    const tables = (tableRows as any[]).map(r => Object.values(r)[0] as string);

    for (let i = 0; i < tables.length; i++) {
      const tbl = tables[i];
      try {
        const [cntRows] = await connection.query(`SELECT COUNT(*) as count FROM \`${tbl}\``);
        const count = (cntRows as any[])[0]?.count ?? 0;
        console.log(`  ${String(i + 1).padStart(2, ' ')}. [${tbl}] - ${count} row(s)`);
      } catch {
        console.log(`  ${String(i + 1).padStart(2, ' ')}. [${tbl}]`);
      }
    }

    console.log('============================================================');
    console.log(`🎉 Total Tables Created: ${tables.length}`);
    console.log('============================================================');
    console.log('\n✨ Next steps:');
    console.log('   1. Start Web Application:    npm run dev  (or double-click run.bat)');
    console.log('   2. Start Java REST Backend:  npm run java:backend (or double-click run_java_backend.bat)');
    console.log('============================================================\n');
  } catch (err: any) {
    console.error('❌ Error executing SQL script:', err.message);
  } finally {
    if (connection) await connection.end();
  }
}

main();
