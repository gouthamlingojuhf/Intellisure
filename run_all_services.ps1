# Launch all IntelliSure backend services in separate terminal windows with managed heap memory

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot

$services = @(
    @{ Name = 'eureka'; Port = 8761; Jar = 'eureka\target\eureka-0.0.1-SNAPSHOT.jar'; Delay = 12 },
    @{ Name = 'api-gateway'; Port = 8080; Jar = 'api-gateway\target\api-gateway-0.0.1-SNAPSHOT.jar'; Delay = 10 },
    @{ Name = 'customer-party-service'; Port = 8081; Jar = 'customer-party-service\target\customer-party-service-0.0.1-SNAPSHOT.jar'; Delay = 5 },
    @{ Name = 'quote-policy-service'; Port = 8082; Jar = 'quote-policy-service\target\quote-policy-service-0.0.1-SNAPSHOT.jar'; Delay = 5 },
    @{ Name = 'risk-underwriting-service'; Port = 8083; Jar = 'risk-underwriting-service\target\risk-underwriting-service-0.0.1-SNAPSHOT.jar'; Delay = 5 },
    @{ Name = 'claims-service'; Port = 8084; Jar = 'claims-service\target\claims-service-0.0.1-SNAPSHOT.jar'; Delay = 5 },
    @{ Name = 'vendor-partner-service'; Port = 8085; Jar = 'vendor-partner-service\target\vendor-partner-service-0.0.1-SNAPSHOT.jar'; Delay = 5 },
    @{ Name = 'recovery-service'; Port = 8086; Jar = 'recovery-service\target\recovery-service-0.0.1-SNAPSHOT.jar'; Delay = 5 },
    @{ Name = 'workflow-notification-service'; Port = 8087; Jar = 'workflow-notification-service\target\workflow-notification-service-0.0.1-SNAPSHOT.jar'; Delay = 5 },
    @{ Name = 'document-audit-service'; Port = 8088; Jar = 'document-audit-service\target\document-audit-service-0.0.1-SNAPSHOT.jar'; Delay = 5 },
    @{ Name = 'analytics-intelligence-service'; Port = 8089; Jar = 'analytics-intelligence-service\target\analytics-intelligence-service-0.0.1-SNAPSHOT.jar'; Delay = 5 }
)

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host " Launching 11 IntelliSure Backend Services       " -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

foreach ($svc in $services) {
    $jarPath = Join-Path $root $svc.Jar
    if (-not (Test-Path $jarPath)) {
        Write-Error "JAR not found: $jarPath. Please run mvn package -DskipTests first."
        return
    }

    $title = "$($svc.Name) (Port $($svc.Port))"
    $cmd = "`$host.UI.RawUI.WindowTitle = '$title'; Set-Location '$root'; java -Xms128m -Xmx256m -jar '$jarPath'"
    
    Write-Host "Starting $($svc.Name) on port $($svc.Port)..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd
    
    Start-Sleep -Seconds $svc.Delay
}

Write-Host ""
Write-Host "All 11 services have been launched in separate windows!" -ForegroundColor Green
Write-Host "Check that Eureka (:8761) and Gateway (:8080) are up, then let the agent continue." -ForegroundColor Green
