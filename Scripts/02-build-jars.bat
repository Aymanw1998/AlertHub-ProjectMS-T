@echo off
setlocal EnableDelayedExpansion

set "PROJECT_ROOT=%CD%"
set SERVICES=SecurityMS UserMS GatewayMS LoaderMS MetricMS ActionMS ProcessorMS EmailMS SmsMS LoggerMS EvaluationMS

echo.
echo ===============================
echo STEP 2 - Build all JAR files
echo ===============================

for %%S in (%SERVICES%) do (
    echo.
    echo Building JAR for %%S
    echo -------------------------------

    if not exist "%PROJECT_ROOT%\..\src\backend\%%S" (
        echo Folder %%S not found.
        exit /b 1
    )

    cd /d "%PROJECT_ROOT%\..\src\backend\%%S"

    call mvnw.cmd clean package
    if errorlevel 1 (
        echo Maven build failed in %%S.
        exit /b 1
    )

    if not exist "target\*.jar" (
        echo JAR file was not created for %%S.
        exit /b 1
    )
)

cd /d "%PROJECT_ROOT%"

echo.
echo ===============================
echo STEP 2 DONE - All JAR files created
echo ===============================

exit /b 0
