# LankaStay Local Development - Start Backend Server
$envFile = Join-Path $PSScriptRoot '.env'
if (Test-Path -LiteralPath $envFile) {
    foreach ($line in Get-Content -LiteralPath $envFile) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith('#')) { continue }
        $parts = $trimmed.Split('=', 2)
        if ($parts.Count -eq 2) {
            [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1], 'Process')
        }
    }
}

Write-Host "Starting LankaStay Spring Boot Backend on http://localhost:8080..." -ForegroundColor Green
Set-Location "$PSScriptRoot\backend"
$mavenRepository = Join-Path $PSScriptRoot 'backend\.m2\repository'
.\mvnw.cmd "-Dmaven.repo.local=$mavenRepository" "-Dmaven.test.skip=true" spring-boot:run
