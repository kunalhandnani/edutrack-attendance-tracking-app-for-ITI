$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$python = "python"

Push-Location $root
try {
    & $python -m pip install -r ".\backend\requirements.txt"
    & $python ".\backend\seed_mongodb.py"
} finally {
    Pop-Location
}
