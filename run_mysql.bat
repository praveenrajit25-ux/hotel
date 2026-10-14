@echo off
TITLE KR Luxe Hotel - MySQL Runner & Table Viewer
color 0B

echo ============================================================
echo   🐬 KR LUXE HOTEL - MYSQL RUNNER & TABLE VIEWER
echo ============================================================
echo.
echo Running MySQL database runner via Node.js direct connection.
echo (This directly connects to localhost:3306 without needing mysql.exe in PATH)
echo.

call npm run mysql

if %errorlevel% neq 0 (
    echo.
    echo ============================================================
    echo [TIP] If MySQL Server is not currently running:
    echo   1. Start MySQL from XAMPP Control Panel or Windows Services
    echo   2. Or run the zero-config built-in SQLite database with:
    echo      npm run db:reset
    echo ============================================================
)

echo.
pause
