@echo off
TITLE KR Luxe Hotel - Database Reset Tool
color 0E

echo ============================================================
echo   KR LUXE HOTEL & RESORT - SQL DATABASE RESET TOOL
echo ============================================================
echo.
echo This will reset database/hotel_management.db to fresh state:
echo   - Cleans all dummy reservations and invoices
echo   - Prepares all 9 luxury rooms as 'Available'
echo   - Restores service catalog and staff directory
echo.

call npm run db:reset

echo.
pause
