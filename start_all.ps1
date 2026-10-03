$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$services = @(
    'eureka',
    'api-gateway',
    'customer-party-service',
    'quote-policy-service',
    'risk-underwriting-service',
    'claims-service',
    'vendor-partner-service',
    'recovery-service',
    'workflow-notification-service',
    'document-audit-service',
    'analytics-intelligence-service'
)

Write-Host 'Starting IntelliSure backend services...' -ForegroundColor Cyan

foreach ($service in $services) {
    $servicePath = Join-Path $root $service
    if (-not (Test-Path $servicePath)) {
        throw "Service folder not found: $servicePath"
    }

    $command = @"
Set-Location '$servicePath'
./mvnw.cmd spring-boot:run
"@

    Write-Host "Launching $service..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList '-NoExit', '-Command', $command | Out-Null

    if ($service -eq 'eureka') {
        Start-Sleep -Seconds 12
    }
    elseif ($service -eq 'api-gateway') {
        Start-Sleep -Seconds 10
    }
    else {
        Start-Sleep -Seconds 5
    }
}

Write-Host ''
Write-Host 'All services have been launched in separate PowerShell windows.' -ForegroundColor Green
Write-Host 'Eureka should come up first, then the API Gateway, then the remaining services.' -ForegroundColor Green
Write-Host 'If you want to stop them, close each terminal window manually.' -ForegroundColor Green
