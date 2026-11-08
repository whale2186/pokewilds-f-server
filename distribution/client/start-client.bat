@echo off
setlocal
set "ROOT=%~dp0"
set "JAVA=java"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java.exe"
if not defined PW_RUNTIME set "PW_RUNTIME=%ROOT%runtime"
if not exist "%PW_RUNTIME%\tmp" mkdir "%PW_RUNTIME%\tmp"
if not exist "%PW_RUNTIME%\settings.txt" copy "%ROOT%config\settings.txt" "%PW_RUNTIME%\settings.txt" >nul
cd /d "%PW_RUNTIME%"
"%JAVA%" "-Djava.io.tmpdir=%PW_RUNTIME%\tmp" -cp "%ROOT%lib\pokewilds-engine.jar;%ROOT%lib\runtime-deps.jar" com.pkmngen.game.desktop.DesktopLauncher %*
if errorlevel 1 pause
