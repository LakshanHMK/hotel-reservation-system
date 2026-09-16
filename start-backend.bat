@echo off
for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%~dp0.env") do set "%%A=%%B"

echo Starting LankaStay Backend on http://localhost:8080...
cd /d "%~dp0backend"
call mvnw.cmd "-Dmaven.repo.local=%~dp0backend\.m2\repository" "-Dmaven.test.skip=true" spring-boot:run
