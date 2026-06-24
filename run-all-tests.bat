@echo off
setlocal

set SERVICES=SecurityMS UserMS GatewayMS LoaderMS MetricMS ActionMS ProcessorMS EmailMS SmsMS LoggerMS EvaluationMS

for %%S in (%SERVICES%) do (
    echo.
    echo ===============================
    echo Running tests for %%S
    echo ===============================

    cd %%S
    call mvnw.cmd clean test
    if errorlevel 1 (
        echo.
        echo Tests failed in %%S
        cd ..
        exit /b 1
    )
    cd ..
)

echo.
echo ===============================
echo ALL TESTS PASSED
echo ===============================
pause
