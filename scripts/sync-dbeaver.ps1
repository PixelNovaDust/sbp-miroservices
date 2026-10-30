# Writes DBeaver project connections from env/.env.dev (one-click connect in Database Navigator).
param(
    [string]$EnvFile = "env/.env.dev"
)

$ErrorActionPreference = "Stop"
$RepoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $RepoRoot

if (-not (Test-Path $EnvFile)) {
    Write-Error "Missing $EnvFile - copy env/.env.dev.example to env/.env.dev first."
}

function Read-DotEnv {
    param([string]$Path)
    $map = @{}
    Get-Content -LiteralPath $Path | ForEach-Object {
        $line = $_.Trim()
        if ($line.Length -eq 0 -or $line.StartsWith("#")) { return }
        $eq = $line.IndexOf("=")
        if ($eq -lt 1) { return }
        $key = $line.Substring(0, $eq).Trim()
        $val = $line.Substring($eq + 1).Trim()
        if ($val.Length -ge 2 -and $val.StartsWith('"') -and $val.EndsWith('"')) {
            $val = $val.Substring(1, $val.Length - 2)
        }
        $map[$key] = $val
    }
    return $map
}

function Get-EnvRequired {
    param([hashtable]$Env, [string]$Key)
    if (-not $Env.ContainsKey($Key) -or [string]::IsNullOrWhiteSpace($Env[$Key])) {
        throw "Missing or empty $Key in $EnvFile"
    }
    return $Env[$Key]
}

function Write-Utf8NoBom {
    param([string]$Path, [string]$Content)
    $utf8 = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($Path, $Content, $utf8)
}

function Encode-UriComponent {
    param([string]$Value)
    return [System.Uri]::EscapeDataString($Value)
}

$envMap = Read-DotEnv -Path $EnvFile

$databases = @(
    @{ Prefix = "AUTH"; ConnectionId = "postgres-jdbc-sbp-dev-auth"; Label = "SBP Auth (dev)" }
    @{ Prefix = "USER"; ConnectionId = "postgres-jdbc-sbp-dev-user"; Label = "SBP User (dev)" }
    @{ Prefix = "PERSON"; ConnectionId = "postgres-jdbc-sbp-dev-person"; Label = "SBP Person (dev)" }
    @{ Prefix = "STUDIO"; ConnectionId = "postgres-jdbc-sbp-dev-studio"; Label = "SBP Studio (dev)" }
    @{ Prefix = "CONTENT"; ConnectionId = "postgres-jdbc-sbp-dev-content"; Label = "SBP Content (dev)" }
    @{ Prefix = "REFERENCE"; ConnectionId = "postgres-jdbc-sbp-dev-reference"; Label = "SBP Reference (dev)" }
)

$connections = @{}

foreach ($db in $databases) {
    $p = $db.Prefix
    $dbHost = Get-EnvRequired $envMap "${p}_DB_HOST"
    $port = Get-EnvRequired $envMap "${p}_DB_PORT"
    $dbName = Get-EnvRequired $envMap "${p}_DB_NAME"
    $user = Get-EnvRequired $envMap "${p}_DB_USERNAME"
    $pass = Get-EnvRequired $envMap "${p}_DB_PASSWORD"
    $id = $db.ConnectionId
    $encUser = Encode-UriComponent $user
    $encPass = Encode-UriComponent $pass
    $jdbcUrl = "jdbc:postgresql://${dbHost}:${port}/${dbName}?user=${encUser}&password=${encPass}"

    $conn = [ordered]@{
        provider    = "postgresql"
        driver      = "postgres-jdbc"
        name        = $db.Label
        description = "Local dev Postgres ($p) from $EnvFile"
        folder      = "SBP Dev"
    }
    # false: use auth-properties in this file; true + broken credentials-config.json => SCRAM with no password
    $conn["save-password"] = $false
    $conn["read-only"] = $false
    $cfg = [ordered]@{
        host              = $dbHost
        port              = [string]$port
        database          = $dbName
        url               = $jdbcUrl
        configurationType = "URL"
        type              = "dev"
        properties        = @{
            connectTimeout = "20"
            loginTimeout   = "20"
            user           = $user
            password       = $pass
        }
        "auth-model"      = "native"
        "auth-properties" = @{
            userName     = $user
            userPassword = $pass
        }
    }
    $conn["configuration"] = $cfg
    $connections[$id] = $conn
}

$devType = [ordered]@{
    name        = "Development"
    color       = "rgb(0,128,0)"
    description = "Local SBP dev databases"
}
$devType["auto-commit"] = $true
$devType["confirm-execute"] = $false
$devType["confirm-data-change"] = $false
$devType["auto-close-transactions"] = $false

$dataSources = [ordered]@{
    folders     = @{}
    connections = $connections
}
$dataSources["connection-types"] = @{ dev = $devType }

$dbeaverDir = Join-Path $RepoRoot ".dbeaver"
New-Item -ItemType Directory -Force -Path $dbeaverDir | Out-Null

$dataSourcesPath = Join-Path $dbeaverDir "data-sources.json"
$credentialsPath = Join-Path $dbeaverDir "credentials-config.json"
$metadataPath = Join-Path $dbeaverDir "project-metadata.json"
$settingsPath = Join-Path $dbeaverDir "project-settings.json"

$json = $dataSources | ConvertTo-Json -Depth 12 -Compress:$false
# ConvertTo-Json escapes & as \u0026; PostgreSQL JDBC URLs require literal &
$json = $json -replace '\\u0026', '&'
Write-Utf8NoBom -Path $dataSourcesPath -Content $json

if (Test-Path $credentialsPath) {
    Remove-Item -LiteralPath $credentialsPath -Force
}

@{
    id          = "sbp-miroservices"
    name        = "sbp-miroservices"
    description = "SBP microservices dev Postgres connections"
} | ConvertTo-Json | ForEach-Object { Write-Utf8NoBom -Path $metadataPath -Content $_ }

@{
    id = "sbp-miroservices"
} | ConvertTo-Json | ForEach-Object { Write-Utf8NoBom -Path $settingsPath -Content $_ }

Write-Host "DBeaver project updated: $dbeaverDir"
Write-Host "Removed stale credentials-config.json (if any)."
Write-Host ""
Write-Host "In DBeaver:"
Write-Host "  1. Project - Refresh (or restart DBeaver)"
Write-Host "  2. Active project must be sbp-miroservices pointing at: $RepoRoot"
Write-Host "  3. Delete duplicate SBP Dev connections under the General project if present"
Write-Host "  4. Connect from SBP Dev folder under sbp-miroservices only"
