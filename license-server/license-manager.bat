@echo off
title LobbySystem Lizenz-Manager

cd /d "%~dp0"
if exist "license-server\server.js" cd /d "%~dp0license-server"

set "API_URL=http://localhost:3000"
set "OWNER=SujucDerBreite"

:MENU
cls
echo ========================================================
echo LobbySystem Lizenz-Manager (%OWNER%)
echo Ordner: %CD%
echo ========================================================
echo.
echo [1] Lizenz-Server in eigenem Fenster starten (Port 3000)
echo [2] Neuen Lizenz-Key erstellen
echo [3] Gespeicherte Lizenzen anzeigen (licenses.json)
echo [4] Beenden
echo.
set /p "CHOICE=Auswahl (1-4): "

if "%CHOICE%"=="1" goto START_SERVER
if "%CHOICE%"=="2" goto CREATE_KEY
if "%CHOICE%"=="3" goto SHOW_LICENSES
if "%CHOICE%"=="4" exit /b
goto MENU

:START_SERVER
cls
if not exist "package.json" (
    echo FEHLER: package.json nicht gefunden!
    pause
    goto MENU
)
if not exist "node_modules" (
    echo Installiere express...
    call npm install
)
echo Starte Lizenz-Server in einem eigenen Fenster auf Port 3000...
start "LobbySystem Lizenz-Server (Port 3000)" cmd /k "cd /d "%CD%" && node server.js"
timeout /t 1 /nobreak >nul
goto MENU

:CREATE_KEY
cls
set /p "CUSTOMER=Kundenname eingeben: "
if "%CUSTOMER%"=="" set "CUSTOMER=Unbekannt"
echo.
echo Erstelle Lizenz fuer "%CUSTOMER%"...
echo.
node -e "const fs=require('fs');const txt=fs.readFileSync('server.js','utf8');const s=txt.split('OWNER_MASTER_SECRET')[1].split('=')[1].split(';')[0].trim().slice(1,-1);fetch('%API_URL%/api/v1/create',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({owner:'%OWNER%',secret:s,customer:process.argv[1]})}).then(r=>r.text()).then(t=>console.log(t)).catch(e=>console.error(e));" "%CUSTOMER%"
echo.
pause
goto MENU

:SHOW_LICENSES
cls
if exist "licenses.json" (
    type licenses.json
) else (
    echo Noch keine licenses.json vorhanden.
)
echo.
echo.
pause
goto MENU
