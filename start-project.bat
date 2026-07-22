@echo off
setlocal
cd /d "%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-project.ps1"
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
  echo.
  echo Startup failed. Review the error above and the logs under %%LOCALAPPDATA%%\mall-test-system.
)

echo.
pause
exit /b %EXIT_CODE%
