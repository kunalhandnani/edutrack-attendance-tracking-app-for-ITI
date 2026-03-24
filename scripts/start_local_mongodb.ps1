$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$mongoExe = "C:\Program Files\MongoDB\Server\8.2\bin\mongod.exe"
$dataDir = Join-Path $root ".mongodb\data"
$logDir = Join-Path $root ".mongodb\logs"
$logFile = Join-Path $logDir "mongod.log"

New-Item -ItemType Directory -Force -Path $dataDir | Out-Null
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$alreadyRunning = Get-Process mongod -ErrorAction SilentlyContinue
if ($alreadyRunning) {
    Write-Output "mongod is already running"
    exit 0
}

Start-Process -FilePath $mongoExe -ArgumentList @(
    "--dbpath", $dataDir,
    "--bind_ip", "127.0.0.1",
    "--port", "27017",
    "--logpath", $logFile
) -WindowStyle Hidden

Start-Sleep -Seconds 3
Write-Output "MongoDB started at mongodb://127.0.0.1:27017"
