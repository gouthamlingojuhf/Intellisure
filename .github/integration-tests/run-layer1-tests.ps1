$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "../..")

docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker is not running. Start Docker before running Layer 1 tests."
}

$modules = @(
    "customer-party-service", "claims-service", "document-audit-service",
    "quote-policy-service", "recovery-service", "vendor-partner-service",
    "workflow-notification-service"
)
$status = 0
foreach ($module in $modules) {
    Write-Host "Running Layer 1 tests in $module"
    Push-Location $module
    try { & .\mvnw.cmd verify "-Dgroups=Layer1"; if ($LASTEXITCODE -ne 0) { $status = $LASTEXITCODE } }
    finally { Pop-Location }
}

if ($status -eq 0) { Write-Host "✅ Layer 1 integration tests PASSED" }
else { Write-Host "❌ Layer 1 integration tests FAILED" }
exit $status
