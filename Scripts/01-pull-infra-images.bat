@echo off
setlocal EnableDelayedExpansion

set INFRA_IMAGES=apache/kafka:4.3.1 mongo:8.3.4 mysql:8.0

echo.
echo ===============================
echo STEP 1 - Pull infrastructure images
echo ===============================

docker info >nul 2>&1
if errorlevel 1 (
    echo Docker Desktop is not running.
    exit /b 1
)

for %%I in (%INFRA_IMAGES%) do (
    echo.
    echo Checking %%I
    docker image inspect %%I >nul 2>&1

    if errorlevel 1 (
        echo Image %%I not found locally. Pulling...
        docker pull %%I

        if errorlevel 1 (
            echo Failed to pull %%I.
            exit /b 1
        )
    ) else (
        echo Image %%I already exists locally.
    )
)

echo.
echo ===============================
echo STEP 1 DONE - Infrastructure images are ready
echo ===============================

exit /b 0
