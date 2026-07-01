@echo off
setlocal EnableDelayedExpansion

set "PROJECT_ROOT=%CD%"
set DOCKER_USER=209138155
set TAG=v5

set SERVICES=SecurityMS:alerthub-security-ms UserMS:alerthub-user-ms GatewayMS:alerthub-gateway-ms LoaderMS:alerthub-loader-ms MetricMS:alerthub-metric-ms ActionMS:alerthub-action-ms ProcessorMS:alerthub-processor-ms EmailMS:alerthub-email-ms SmsMS:alerthub-sms-ms LoggerMS:alerthub-logger-ms EvaluationMS:alerthub-evaluation-ms

echo.
echo ===============================
echo STEP 4 - Push all Docker images
echo ===============================

docker info >nul 2>&1
if errorlevel 1 (
    echo Docker Desktop is not running.
    exit /b 1
)

docker info | findstr /C:"Username:" >nul 2>&1
if errorlevel 1 (
    echo You are not logged in to Docker Hub.
    docker login -u %DOCKER_USER%

    if errorlevel 1 (
        echo Docker login failed.
        exit /b 1
    )
)


for %%P in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%A in ("%%P") do (
        set IMAGE_NAME=%DOCKER_USER%/%%B:%TAG%

        echo.
        echo Pushing Docker image for %%A
        echo Image: !IMAGE_NAME!
        echo -------------------------------

        docker image inspect !IMAGE_NAME! >nul 2>&1
        if errorlevel 1 (
            echo Missing local image for %%A. Run 02-build-images.bat first.
            exit /b 1
        )

        docker push !IMAGE_NAME!
        if errorlevel 1 (
            echo Docker push failed for %%A.
            exit /b 1
        )
    )
)

cd /d "%PROJECT_ROOT%"

echo.
echo ===============================
echo STEP 4 DONE - All Docker images pushed
echo ===============================

exit /b 0
