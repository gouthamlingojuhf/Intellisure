# IntelliSure Local Database Initialization Script (No Docker)
# Creates the 9 microservice databases and seeds all baseline demo accounts

param(
    [string]$User = "root",
    [string]$Password = ""
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host " IntelliSure Local MySQL Database Setup         " -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# Check if mysql client is available
$mysqlCmd = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysqlCmd) {
    Write-Host "WARNING: 'mysql' command line client not found in PATH." -ForegroundColor Yellow
    Write-Host "Please ensure MySQL is installed and added to your PATH, or execute" -ForegroundColor Yellow
    Write-Host "the SQL scripts inside MySQL Workbench / DBeaver / HeidiSQL / phpMyAdmin:" -ForegroundColor Yellow
    Write-Host "  1. $root\docker\mysql\init\00-create-databases.sql" -ForegroundColor White
    Write-Host "  2. $root\docker\mysql\init\reset_and_seed.sql" -ForegroundColor White
    return
}

$pwArgs = @("-u", $User)
if ($Password) {
    $pwArgs += "-p$Password"
} else {
    Write-Host "Connecting to MySQL as '$User' with no password (press Enter if prompted)..." -ForegroundColor Yellow
}

# 1. Create the 9 databases
Write-Host "1. Creating the 9 microservice databases..." -ForegroundColor Yellow
$createDbFile = Join-Path $root "docker\mysql\init\00-create-databases.sql"
Get-Content $createDbFile | & mysql @pwArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to create databases. Please check your MySQL credentials."
    return
}
Write-Host "   -> Databases created successfully!" -ForegroundColor Green

# 2. Apply column migrations / fixes if tables already exist
Write-Host "2. Verifying and applying schema column migrations..." -ForegroundColor Yellow
$fixFile = Join-Path $root "docker\mysql\init\fix_missing_columns.sql"
if (Test-Path $fixFile) {
    Get-Content $fixFile | & mysql @pwArgs
    Write-Host "   -> Column migrations verified!" -ForegroundColor Green
}

# 3. Seed baseline accounts and accredited vendors
Write-Host "3. Seeding demo accounts and master configuration..." -ForegroundColor Yellow
$seedFile = Join-Path $root "docker\mysql\init\reset_and_seed.sql"
Get-Content $seedFile | & mysql @pwArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to seed database records."
    return
}
Write-Host "   -> Demo accounts, vendors, and risk rules seeded successfully!" -ForegroundColor Green

Write-Host ""
Write-Host "Database setup complete! You can now start the backend services with .\run_all_services.ps1" -ForegroundColor Cyan
