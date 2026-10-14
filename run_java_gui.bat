@echo off
TITLE KR Luxe Hotel - Java Desktop System
color 0E

echo ============================================================
echo   ☕ KR LUXE HOTEL - JAVA DESKTOP SYSTEM LAUNCHER
echo ============================================================
echo.

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed. Please install JDK 17+ from https://adoptium.net/
    pause
    exit /b 1
)

cd HotelManagementSystem
call mvn compile exec:java -Dexec.mainClass="com.hotelmanagement.Main"
pause
