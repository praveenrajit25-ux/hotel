@echo off
TITLE KR Luxe Hotel - Java REST Backend Server
color 0A

echo ============================================================
echo   ☕ KR LUXE HOTEL - JAVA REST BACKEND SERVER LAUNCHER
echo ============================================================
echo.

:: 1. Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java (JDK 17+) is not recognized on your system.
    echo Please download and install JDK 17+ from: https://adoptium.net/
    pause
    exit /b 1
)

:: 2. Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [INFO] Maven not found in PATH, trying to run using direct javac/java...
    cd HotelManagementSystem
    if not exist target\classes (
        mkdir target\classes 2>nul
    )
    javac -d target\classes -sourcepath src\main\java src\main\java\com\hotelmanagement\BackendMain.java
    java -cp target\classes com.hotelmanagement.BackendMain
    pause
    exit /b
)

echo [INFO] Compiling and starting Java Backend Server on port 8080...
cd HotelManagementSystem
call mvn compile exec:java -Dexec.mainClass="com.hotelmanagement.BackendMain"

pause
