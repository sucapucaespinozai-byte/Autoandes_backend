@echo off
cd /d "%~dp0"
if exist "%USERPROFILE%\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2\bin\java.exe" set "JAVA_HOME=%USERPROFILE%\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2"
call mvnw.cmd spring-boot:run
pause
