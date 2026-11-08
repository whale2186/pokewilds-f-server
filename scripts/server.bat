@echo off
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "ROOT=%%~fI"

call "%SCRIPT_DIR%dev.bat" build
if !ERRORLEVEL! neq 0 (
    echo Build failed. >&2
    exit /b !ERRORLEVEL!
)

if not defined PW_RUNTIME (
    set "RUNTIME=%ROOT%\runtime"
) else (
    set "RUNTIME=%PW_RUNTIME%"
)

if not exist "%RUNTIME%" mkdir "%RUNTIME%"
cd /d "%RUNTIME%"

set "JAVA_CMD="
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

if not defined JAVA_CMD (
    where java >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        set "JAVA_CMD=java"
    )
)

if not defined JAVA_CMD (
    echo Error: java not found. Set JAVA_HOME or add JDK/JRE to PATH. >&2
    exit /b 1
)

"%JAVA_CMD%" -Djava.awt.headless=true -cp "%ROOT%\build\pokewilds-engine.jar;%ROOT%\vendor\runtime-deps.jar" com.pkmngen.game.DedicatedServer %*
exit /b !ERRORLEVEL!
