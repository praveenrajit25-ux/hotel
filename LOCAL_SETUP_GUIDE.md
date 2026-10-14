# KR LUXE HOTEL - STEP-BY-STEP MANUAL & 1-CLICK RUN GUIDE

This project includes:
1. **Web Hotel Application (React + Node.js)**
2. **SQL Database System (MySQL & SQLite)**
3. **Java Backend REST Server & Desktop System (`HotelManagementSystem`)**

---

## 🚀 Manual Step-by-Step Execution (Terminal / Command Prompt)

Follow these 4 simple manual steps in your project folder:

### Step 1: Run NPM Manually
```bash
npm install
npm run dev
```
👉 Your Web Application will start on **`http://localhost:3000`**.

---

### Step 2: Reset Database Tables Manually
```bash
npm run reset
```
👉 This resets the database with:
- Zero dummy bookings
- All 9 hotel rooms marked **Available** for real bookings
- Full services catalog & staff directory

---

### Step 3: Run the MySQL SQL File & Show Tables
*(Direct Node.js TCP connection — **No `mysql.exe` needed in Windows PATH**!)*
```bash
npm run mysql
```
When prompted for your password:
```text
🔑 Enter MySQL root password (press Enter for 'Vigensh1@.'):
```
Press **Enter** (it defaults to your password `Vigensh1@.`), and it will:
1. Connect to MySQL on `localhost:3306`
2. Execute `HotelManagementSystem/database/hotel_management.sql`
3. Execute `SHOW TABLES;` and print all created tables with their row counts!

*(Optional: To run SQLite queries directly from terminal anytime:)*
```bash
npm run sql
npm run sql "SELECT * FROM rooms;"
npm run sql "SELECT * FROM reservations;"
```

---

### Step 4: Run the Java Backend Manually
```bash
npm run java
```
👉 Starts the Java REST API server on **`http://localhost:8080`**.
*(Or launch the Java Swing GUI: `npm run java:gui`)*

---

## ⚡ 1-Click Windows Launchers (Double-Click in File Explorer)

You can also run everything by double-clicking:

| File | Purpose |
| :--- | :--- |
| **`run.bat`** | Starts Web App on `http://localhost:3000` |
| **`run_mysql.bat`** | Runs MySQL setup with your password & displays tables |
| **`run_java_backend.bat`** | Starts Java REST API Server on `http://localhost:8080` |
| **`run_java_gui.bat`** | Launches Java Desktop Swing GUI |
| **`reset-db.bat`** | Resets database tables |
