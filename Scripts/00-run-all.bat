@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

for /f %%T in ('powershell -NoProfile -Command "[DateTimeOffset]::Now.ToUnixTimeSeconds()"') do set START_SECONDS=%%T

echo.
echo START TIME: %date% %time%
echo.

call 01-pull-infra-images.bat
if errorlevel 1 (
    echo.
    echo Process stopped in STEP 0.
    pause
    exit /b 1
)

call 02-build-jars.bat
if errorlevel 1 (
    echo.
    echo Process stopped in STEP 1.
    pause
    exit /b 1
)

call 03-build-images.bat
if errorlevel 1 (
    echo.
    echo Process stopped in STEP 2.
    pause
    exit /b 1
)

call 04-push-images.bat
if errorlevel 1 (
    echo.
    echo Process stopped in STEP 3.
    pause
    exit /b 1
)

for /f %%T in ('powershell -NoProfile -Command "[DateTimeOffset]::Now.ToUnixTimeSeconds()"') do set END_SECONDS=%%T

set /a TOTAL_SECONDS=!END_SECONDS!-!START_SECONDS!
set /a TOTAL_MINUTES=!TOTAL_SECONDS!/60
set /a TOTAL_REMAIN_SECONDS=!TOTAL_SECONDS!%%60

echo.
echo END TIME: %date% %time%
echo.
echo ===============================
echo ALL STEPS COMPLETED SUCCESSFULLY
echo TOTAL RUNTIME: !TOTAL_MINUTES! minutes and !TOTAL_REMAIN_SECONDS! seconds
echo ===============================
echo.

pause
exit /b 0
