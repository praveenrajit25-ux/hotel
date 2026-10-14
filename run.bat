@echo off
TITLE KR Luxe Hotel - Server Launcher
color 0A

echo ============================================================
echo   KR LUXE HOTEL & RESORT - ONE-CLICK LOCAL LAUNCHER
echo ============================================================
echo.

:: 1. Check if Node.js is installed
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is not recognized on your system.
    echo Please install Node.js (LTS version) from: https://nodejs.org
    echo Once installed, reopen this command prompt and run run.bat again.
    pause
    exit /b 1
)

:: 2. Check if dependencies are installed
if not exist node_modules (
    echo [INFO] Installing required dependencies...
    call npm install
    if %errorlevel% neq 0 (
        echo [ERROR] npm install encountered an error.
        pause
        exit /b 1
    )
)

:: 3. Check if database file exists, initialize if missing
if not exist database\hotel_management.db (
    echo [INFO] Initializing SQLite database...
    call npm run db:reset
)

echo.
echo ============================================================
echo  Starting KR Luxe Hotel on http://localhost:3000 ...
echo  Press Ctrl+C to stop the server anytime.
echo ============================================================
echo.

call npm run dev
pause
