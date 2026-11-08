@echo off
setlocal
set "ROOT=%~dp0"
set "JAVA=java"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java.exe"
if exist "%ROOT%jre\bin\java.exe" set "JAVA=%ROOT%jre\bin\java.exe"
set "WORLD=%~1"
if not defined WORLD set "WORLD=shared"
set "PORT=%~2"
if not defined PORT set "PORT=54555"
if not defined PW_RUNTIME set "PW_RUNTIME=%ROOT%runtime"
if not exist "%PW_RUNTIME%" mkdir "%PW_RUNTIME%"
cd /d "%PW_RUNTIME%"
"%JAVA%" -Xmx2g -Djava.awt.headless=true -cp "%ROOT%lib\pokewilds-engine.jar;%ROOT%lib\runtime-deps.jar" com.pkmngen.game.DedicatedServer "%WORLD%" "%PORT%"
if errorlevel 1 pause
