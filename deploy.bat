@echo off
title LobbySystem GitHub Deploy

cd /d "%~dp0"
if not exist ".git" (
    git init -b main
)
git remote remove origin 2>nul
git remote add origin https://github.com/conlog06/lobbysystem-google.git
git add .
git commit -m "Release v1.1.0: LobbySystem 1.21.10-1.26.3 (MiniMessage, Small-Caps & Modern Scoreboard)"
git push -u origin main --force
git tag -f v1.1.0
git push origin v1.1.0 --force
echo Fertig! GitHub Actions baut jetzt das Release unter https://github.com/conlog06/lobbysystem-google/releases
pause
