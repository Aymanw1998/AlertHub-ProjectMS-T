@echo off
setlocal EnableDelayedExpansion

for /f %%T in ('powershell -NoProfile -Command "[DateTimeOffset]::Now.ToUnixTimeSeconds()"') do set START_SECONDS=%%T

echo.
echo START TIME: %date% %time%
echo.


set DOCKER_USER=209138155
set TAG=v1

set SERVICES=SecurityMS:alerthub-security-ms UserMS:alerthub-user-ms GatewayMS:alerthub-gateway-ms LoaderMS:alerthub-loader-ms MetricMS:alerthub-metric-ms ActionMS:alerthub-action-ms ProcessorMS:alerthub-processor-ms EmailMS:alerthub-email-ms SmsMS:alerthub-sms-ms LoggerMS:alerthub-logger-ms EvaluationMS:alerthub-evaluation-ms

echo.
echo ===============================
echo Login to Docker Hub first if needed:
echo docker login -u %DOCKER_USER%
echo ===============================
echo.

for %%P in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%A in ("%%P") do (
        echo.
        echo ===============================
        echo Building jar and Docker image for %%A
        echo Image: %DOCKER_USER%/%%B:%TAG%
        echo ===============================

        cd %%A

        call mvnw.cmd clean package
        if errorlevel 1 (
            echo.
            echo Maven build failed in %%A
            cd ..
            exit /b 1
        )

        docker build -t %DOCKER_USER%/%%B:%TAG% .
        if errorlevel 1 (
            echo.
            echo Docker build failed in %%A
            cd ..
            exit /b 1
        )

        docker push %DOCKER_USER%/%%B:%TAG%
        if errorlevel 1 (
            echo.
            echo Docker push failed in %%A
            cd ..
            exit /b 1
        )

        cd ..
    )
)

for /f %%T in ('powershell -NoProfile -Command "[DateTimeOffset]::Now.ToUnixTimeSeconds()"') do set END_SECONDS=%%T

set /a TOTAL_SECONDS=!END_SECONDS!-!START_SECONDS!
set /a TOTAL_MINUTES=!TOTAL_SECONDS!/60
set /a TOTAL_REMAIN_SECONDS=!TOTAL_SECONDS!%%60

echo.
echo END TIME: %date% %time%
echo.
echo ===============================
echo TOTAL RUNTIME: !TOTAL_MINUTES! minutes and !TOTAL_REMAIN_SECONDS! seconds
echo ===============================
echo.


echo.
echo ===============================
echo ALL JARS, IMAGES, AND PUSHES COMPLETED SUCCESSFULLY
echo ===============================
pause