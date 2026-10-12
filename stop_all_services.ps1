# Stop all IntelliSure backend services running on ports 8761, 8080, and 8081-8089

$ports = 8761, 8080, 8081, 8082, 8083, 8084, 8085, 8086, 8087, 8088, 8089

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host " Stopping IntelliSure Backend Services           " -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

foreach ($port in $ports) {
    try {
        $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
        if ($conns) {
            foreach ($conn in $conns) {
                $procId = $conn.OwningProcess
                if ($procId -gt 0) {
                    Write-Host "Terminating process PID $procId listening on port $port..." -ForegroundColor Yellow
                    Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
                }
            }
        }
    } catch {
        # continue silently
    }
}

Write-Host ""
Write-Host "All IntelliSure backend services have been stopped." -ForegroundColor Green
