@echo off
title PhoneMic Keyboard Server
echo ================================
echo   PhoneMic Keyboard Server
echo ================================
echo.

:: 获取本机IP
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /i "IPv4" ^| findstr "192.168"') do set IP=%%a
set IP=%IP: =%
echo   PC IP: %IP%
echo   Port: 8765
echo   手机连接: %IP%:8765
echo.
echo ================================

cd /d E:\pythonCode\phone_keybord\pc_server
python main.py
pause
