# Seed reference, studio, and person dev catalog (run after migrate dev when services are up).
param(
    [switch]$Reset,
    [switch]$ResetDb,
    [string]$GatewayBase = "http://localhost:8080",
    [string]$ReferenceBase = "",
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$ScriptsDir = Split-Path -Parent $PSScriptRoot
$RepoRoot = Split-Path -Parent $ScriptsDir
Set-Location $RepoRoot
. "$ScriptsDir\lib\service-runtime.ps1"

$EnvDev = Join-Path $RepoRoot "env/.env.dev"
Import-SbpDevEnv -EnvFilePath $EnvDev -RepoRoot $RepoRoot

$GatewayBase = Resolve-SbpGatewayBaseUrl -Candidate $GatewayBase
if ([string]::IsNullOrWhiteSpace($ReferenceBase)) {
    $refPort = if ($env:REFERENCE_PORT) { $env:REFERENCE_PORT } else { "8087" }
    $ReferenceBase = "http://localhost:$refPort"
}

$env:REPO_ROOT = $RepoRoot
$env:ENV_DEV = $EnvDev
$env:GATEWAY_BASE = $GatewayBase
$env:REFERENCE_SERVICE_URL = $ReferenceBase.TrimEnd("/")
$env:SEED_ACTOR = "dev-seed"

$pyArgs = @("$PSScriptRoot\seed-dev-data.py")
if ($ResetDb) { $pyArgs += "--reset-db" }
elseif ($Reset) { $pyArgs += "--reset" }
if ($Force) { $pyArgs += "--force" }

Write-Host "Gateway: $GatewayBase"
Write-Host "Reference (internal entity-taxonomies): $($env:REFERENCE_SERVICE_URL)"
Write-Host "Ensure discovery, gateway, reference, studio, person, and content services are running."

& python @pyArgs
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
