@echo off
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "ROOT=%%~fI"

set "JDK_BIN="
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set "JDK_BIN=%JAVA_HOME%\bin"
    )
)

if not defined JDK_BIN (
    where javac >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        for /f "delims=" %%I in ('where javac') do (
            if not defined JDK_BIN (
                set "JAVAC_FULL=%%~dpI"
                set "JDK_BIN=!JAVAC_FULL:~0,-1!"
            )
        )
    )
)

if defined JDK_BIN (
    set "JAVAC_CMD=%JDK_BIN%\javac.exe"
    set "JAVA_CMD=%JDK_BIN%\java.exe"
    set "JAR_CMD=%JDK_BIN%\jar.exe"
) else (
    set "JAVAC_CMD=javac"
    set "JAVA_CMD=java"
    set "JAR_CMD=jar"
)

set "DEPS=%ROOT%\vendor\runtime-deps.jar"
set "MAIN=com.pkmngen.game.desktop.DesktopLauncher"

set "ACTION=%~1"
if "%ACTION%"=="" goto :usage

if /i "%ACTION%"=="build" goto :action_build
if /i "%ACTION%"=="run" goto :action_run
if /i "%ACTION%"=="debug" goto :action_debug
if /i "%ACTION%"=="test" goto :action_test
if /i "%ACTION%"=="rea" goto :action_rea
goto :usage

:action_build
call :do_build
exit /b !ERRORLEVEL!

:action_run
call :do_build
if !ERRORLEVEL! neq 0 exit /b !ERRORLEVEL!
set "DEBUG_OPTS="
goto :action_launch

:action_debug
call :do_build
if !ERRORLEVEL! neq 0 exit /b !ERRORLEVEL!
set "DEBUG_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:5005"
goto :action_launch

:action_launch
if not defined PW_RUNTIME (
    set "RUNTIME=%ROOT%\runtime"
) else (
    set "RUNTIME=%PW_RUNTIME%"
)

if not exist "%RUNTIME%\cache" mkdir "%RUNTIME%\cache"
if not exist "%RUNTIME%\config" mkdir "%RUNTIME%\config"
if not exist "%RUNTIME%\data" mkdir "%RUNTIME%\data"
if not exist "%RUNTIME%\tmp" mkdir "%RUNTIME%\tmp"

if not exist "%RUNTIME%\settings.txt" (
    if exist "%ROOT%\vendor\pokewilds-v0.8.11-otherplatforms\settings.txt" (
        copy /y "%ROOT%\vendor\pokewilds-v0.8.11-otherplatforms\settings.txt" "%RUNTIME%\settings.txt" >nul
    )
)

cd /d "%RUNTIME%"

set "REST="
for /f "tokens=1* delims= " %%A in ("%*") do (
    set "REST=%%B"
)

"%JAVA_CMD%" -Djava.io.tmpdir="%RUNTIME%\tmp" !DEBUG_OPTS! -cp "%ROOT%\build\pokewilds-engine.jar;%DEPS%" %MAIN% !REST!
exit /b !ERRORLEVEL!

:action_test
where python >nul 2>&1
if !ERRORLEVEL! equ 0 (
    python -m unittest discover -s "%ROOT%\tests" -v
    exit /b !ERRORLEVEL!
)
where py >nul 2>&1
if !ERRORLEVEL! equ 0 (
    py -m unittest discover -s "%ROOT%\tests" -v
    exit /b !ERRORLEVEL!
)
where python3 >nul 2>&1
if !ERRORLEVEL! equ 0 (
    python3 -m unittest discover -s "%ROOT%\tests" -v
    exit /b !ERRORLEVEL!
)
echo Error: python not found in PATH. >&2
exit /b 1

:action_rea
set "REST="
for /f "tokens=1* delims= " %%A in ("%*") do (
    set "REST=%%B"
)
if exist "%ROOT%\tools\rea-cli\node_modules\.bin\rea.cmd" (
    call "%ROOT%\tools\rea-cli\node_modules\.bin\rea.cmd" !REST!
    exit /b !ERRORLEVEL!
)
if exist "%ROOT%\tools\rea-cli\node_modules\rea-agents\scripts\rea.mjs" (
    node "%ROOT%\tools\rea-cli\node_modules\rea-agents\scripts\rea.mjs" !REST!
    exit /b !ERRORLEVEL!
)
echo Error: rea CLI not found in tools/rea-cli. >&2
exit /b 1

:do_build
if not exist "%ROOT%\build" mkdir "%ROOT%\build"
if not exist "%ROOT%\logs" mkdir "%ROOT%\logs"

set "CLASSES_DIR=%ROOT%\build\classes_%RANDOM%%RANDOM%"
if exist "%CLASSES_DIR%" rmdir /s /q "%CLASSES_DIR%"
mkdir "%CLASSES_DIR%"

set "SOURCES_LIST=%ROOT%\build\sources_%RANDOM%%RANDOM%.txt"
if exist "%SOURCES_LIST%" del /f /q "%SOURCES_LIST%"

(for /r "%ROOT%\engine\src\main\java" %%F in (*.java) do (
    echo "%%F"
)) > "%SOURCES_LIST%" 2>nul

for %%A in ("%SOURCES_LIST%") do (
    if %%~zA equ 0 (
        echo No engine Java sources found. >&2
        del /f /q "%SOURCES_LIST%" >nul 2>&1
        rmdir /s /q "%CLASSES_DIR%" >nul 2>&1
        exit /b 1
    )
)

"%JAVAC_CMD%" --release 8 -g -encoding UTF-8 -cp "%DEPS%" -d "%CLASSES_DIR%" @"%SOURCES_LIST%" > "%ROOT%\logs\build.log" 2>&1
if !ERRORLEVEL! neq 0 (
    type "%ROOT%\logs\build.log" >&2
    del /f /q "%SOURCES_LIST%" >nul 2>&1
    rmdir /s /q "%CLASSES_DIR%" >nul 2>&1
    exit /b 1
)
del /f /q "%SOURCES_LIST%" >nul 2>&1

set "ARTIFACT=%ROOT%\build\pokewilds-engine-%RANDOM%%RANDOM%.jar"
"%JAR_CMD%" --create --file "%ARTIFACT%" -C "%CLASSES_DIR%" .
if !ERRORLEVEL! neq 0 (
    "%JAR_CMD%" -cf "%ARTIFACT%" -C "%CLASSES_DIR%" .
    if !ERRORLEVEL! neq 0 (
        echo Error creating jar artifact. >&2
        rmdir /s /q "%CLASSES_DIR%" >nul 2>&1
        exit /b 1
    )
)

move /y "%ARTIFACT%" "%ROOT%\build\pokewilds-engine.jar" >nul
rmdir /s /q "%CLASSES_DIR%" >nul 2>&1
echo Built Java engine: %ROOT%\build\pokewilds-engine.jar
exit /b 0

:usage
echo Usage: %~nx0 {build^|run [dev]^|debug [dev]^|test^|rea ARGS...} >&2
exit /b 1
