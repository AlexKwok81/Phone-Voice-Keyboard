@echo off
title PhoneMic Keyboard Server
echo ================================
echo   PhoneMic Keyboard Server
echo ================================
echo.

:: ????IP
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /i "IPv4" ^| findstr "192.168"') do set IP=%%a
set IP=%IP: =%
echo   PC IP: %IP%
echo   Port: 8765
echo   ????: %IP%:8765
echo.
echo ================================

python main.py
pause
