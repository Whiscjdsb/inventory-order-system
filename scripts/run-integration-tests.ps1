$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

$env:DB_URL = "jdbc:mysql://localhost:3307/inventory?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME = "root"

$passwordLine = Get-Content ".env" |
    Where-Object { $_ -match '^DB_PASSWORD=' } |
    Select-Object -First 1

if ([string]::IsNullOrWhiteSpace($passwordLine)) {
    throw "DB_PASSWORD was not found in .env"
}

$env:DB_PASSWORD = ($passwordLine -replace '^DB_PASSWORD=', '').Trim()

docker compose up -d --wait mysql

if ($LASTEXITCODE -ne 0) {
    throw "Failed to start Docker MySQL. Start Docker Desktop and try again."
}

mvn "-Dtest=ProductConcurrencyIT,OrderTransactionRollbackIT" test

exit $LASTEXITCODE
