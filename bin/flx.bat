@echo off
rem Launcher for Flux Compiler — run as: flx <file.flx>
setlocal
set "BIN_DIR=%~dp0"
echo Running: java -cp "%BIN_DIR%." flx.Compiler %*
java -cp "%BIN_DIR%." flx.Compiler %*
endlocal
