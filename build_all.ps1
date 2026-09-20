$ErrorActionPreference = 'Stop'

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

foreach ($service in $services) {
    Write-Host "Building $service..."
    Push-Location $service
    try {
        & .\mvnw.cmd clean install -DskipTests
        if ($LASTEXITCODE -ne 0) {
            throw "Maven build failed for $service with exit code $LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }
}

Write-Host 'All services built successfully.'
