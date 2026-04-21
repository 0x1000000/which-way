@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "SCRIPT_DIR=%~dp0"
set "APK_PATH=%SCRIPT_DIR%app\build\outputs\apk\profile\app-profile.apk"
set "DEVICE_COUNT=0"
set "IP_DEVICE="
set "SELECTED_DEVICE="

for /f "skip=1 tokens=1,2" %%A in ('adb devices') do (
    if "%%B"=="device" (
        set /a DEVICE_COUNT+=1
        set "SELECTED_DEVICE=%%A"
        echo %%A| findstr /R "^[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*:[0-9][0-9]*$" >nul
        if not errorlevel 1 if not defined IP_DEVICE (
            set "IP_DEVICE=%%A"
        )
    )
)

if %DEVICE_COUNT% EQU 0 (
    echo No connected Android devices found.
    exit /b 1
)

if %DEVICE_COUNT% GTR 1 (
    if not defined IP_DEVICE (
        echo Multiple Android devices found, but none use a direct IP address.
        exit /b 1
    )
    set "SELECTED_DEVICE=%IP_DEVICE%"
)

call gradlew.bat assembleProfile || exit /b 1
adb -s "%SELECTED_DEVICE%" install -r "%APK_PATH%"
