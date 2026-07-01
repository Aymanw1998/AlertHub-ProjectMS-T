@echo off
setlocal EnableDelayedExpansion

set "PROJECT_ROOT=%CD%"
set DOCKER_USER=209138155
set TAG=v4

set SERVICES=SecurityMS:alerthub-security-ms UserMS:alerthub-user-ms GatewayMS:alerthub-gateway-ms LoaderMS:alerthub-loader-ms MetricMS:alerthub-metric-ms ActionMS:alerthub-action-ms ProcessorMS:alerthub-processor-ms EmailMS:alerthub-email-ms SmsMS:alerthub-sms-ms LoggerMS:alerthub-logger-ms EvaluationMS:alerthub-evaluation-ms

echo.
echo ===============================
echo STEP 3 - Build images
echo ===============================

docker info >nul 2>&1
if errorlevel 1 (
    echo Docker Desktop is not running.
    exit /b 1
)

for %%P in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%A in ("%%P") do (
        set IMAGE_NAME=%DOCKER_USER%/%%B:%TAG%

        echo.
        echo Building Docker image for %%A
        echo Image: !IMAGE_NAME!
        echo -------------------------------

        if not exist "%PROJECT_ROOT%\..\src\Backend\%%A\target\*.jar" (
            echo Missing JAR for %%A. Run 01-build-jars.bat first.
            exit /b 1
        )

        cd /d "%PROJECT_ROOT%\..\src\Backend\%%A"

        docker build -t !IMAGE_NAME! .
        if errorlevel 1 (
            echo Docker build failed for %%A.
            exit /b 1
        )

        docker image inspect !IMAGE_NAME! >nul 2>&1
        if errorlevel 1 (
            echo Docker image was not created for %%A.
            exit /b 1
        )
    )
)

cd /d "%PROJECT_ROOT%"

echo.
echo ===============================
echo STEP 3 DONE - All Docker images created
echo ===============================

exit /b 0
