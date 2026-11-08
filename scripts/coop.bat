@echo off
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "ROOT=%%~fI"

set "MODE=%~1"
set "ENDPOINT=%~2"
set "NAME=%~3"

if "%~1"=="" goto :usage
if "%~2"=="" goto :usage
if "%~3"=="" goto :usage

if /i not "!MODE!"=="host" if /i not "!MODE!"=="join" (
    goto :usage
)

set "SAFE_NAME="
for /f "delims=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-" %%A in ("!NAME!") do set "SAFE_NAME=%%A"
if defined SAFE_NAME (
    echo Invalid player name ^(1-24 letters/digits/_/-^). >&2
    exit /b 2
)

if not "!NAME:~24,1!"=="" (
    echo Invalid player name ^(1-24 letters/digits/_/-^). >&2
    exit /b 2
)

set "REST="
for /f "tokens=1,2,3* delims= " %%A in ("%*") do (
    set "REST=%%D"
)

if not defined PW_RUNTIME (
    set "PW_RUNTIME=%ROOT%\runtime\coop-!MODE!"
    if not exist "!PW_RUNTIME!\" (
        set "LEGACY_COUNT=0"
        for /d %%D in ("%ROOT%\runtime\coop-!MODE!-*") do (
            set /a LEGACY_COUNT+=1
            set "LEGACY_PATH=%%~fD"
        )
        if exist "%ROOT%\runtime\coop-!MODE!-!NAME!\" (
            set "PW_RUNTIME=%ROOT%\runtime\coop-!MODE!-!NAME!"
        ) else if !LEGACY_COUNT! equ 1 (
            set "PW_RUNTIME=!LEGACY_PATH!"
        ) else if !LEGACY_COUNT! gtr 1 (
            echo Multiple legacy player folders. Set PW_RUNTIME to your original player folder. >&2
            exit /b 2
        )
    )
)

if defined REST (
    call "%SCRIPT_DIR%dev.bat" run "--coop-!MODE!=!ENDPOINT!" "--coop-name=!NAME!" !REST!
) else (
    call "%SCRIPT_DIR%dev.bat" run "--coop-!MODE!=!ENDPOINT!" "--coop-name=!NAME!"
)
exit /b !ERRORLEVEL!

:usage
echo Usage: %~nx0 {host^|join} ADDRESS:PORT NAME [--coop-new=WORLD^|--coop-world=WORLD] [--coop-size=30000] >&2
exit /b 2
