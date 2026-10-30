# SBP stack commands for Windows (use when make.exe is blocked or unavailable)
param(
    [Parameter(Position = 0)]
    [string]$Target = "help",
    [Parameter(Position = 1)]
    [string]$ServiceName = "",
    [Parameter(Position = 2)]
    [string]$Extra = "",
    [Parameter(ValueFromRemainingArguments = $true)]
    [object[]]$RemainingArgs
)

$ErrorActionPreference = "Stop"
$RepoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $RepoRoot
. "$PSScriptRoot\service-runtime.ps1"

$EnvDev = "env/.env.dev"
$EnvProd = "env/.env.prod"
$ComposeDev = "docker compose -p sbp-dev --env-file $EnvDev -f docker/docker-compose.dev.yaml"
$ComposeProd = "docker compose -p sbp-prod --env-file $EnvProd -f docker/docker-compose.prod.yaml"
$InfraDev = "auth-db user-db person-db studio-db content-db reference-db prometheus grafana"
$InfraProd = $InfraDev
$DbsDev = "auth-db user-db person-db studio-db content-db reference-db"

function Show-Help {
    Write-Host "SBP Microservices - PowerShell targets (scripts/run.ps1)"
    Write-Host ""
    Write-Host "  help                   Show this help"
    Write-Host "  install-lib            Install sbp-lib-common into local ~/.m2"
    Write-Host "  dbeaver-sync           Refresh DBeaver connections from env/.env.dev"
    Write-Host "  migrate-dev            Run Liquibase on dev DBs (DBs must be up)"
    Write-Host "  migrate-dev-repair     Clear checksums then migrate (dev only)"
    Write-Host "  migrate-prod           Run Liquibase on prod DBs"
    Write-Host "  infra-dev              Dev infra + migrations (stack sbp-dev)"
    Write-Host "  start-dev              DBs, migrations, full dev stack"
    Write-Host "  down-dev               Stop sbp-dev stack (keeps DB volumes)"
    Write-Host "  down-dev-volumes       Stop sbp-dev and delete DB volumes"
    Write-Host "  reset-dev              down-dev-volumes + infra-dev (fresh DB passwords)"
    Write-Host "  infra-prod             Prod infra + migrations"
    Write-Host "  start-prod             Full prod stack"
    Write-Host "  down-prod              Stop sbp-prod stack"
    Write-Host ""
    Write-Host "  start <alias>          Start one service (local JVM, env/.env.dev)"
    Write-Host "  start <alias> docker   Start one service via Docker Compose"
    Write-Host "  stop <alias>           Stop local JVM and Docker container for a service"
    Write-Host "  restart <alias>        Stop then start (local JVM; pulls deps discovery/gateway)"
    Write-Host "  restart <alias> docker Stop then start via Docker Compose"
    Write-Host "  svc-info               Port, PID, uptime, status for all services"
    Write-Host "  svc-list               List service aliases (auth, person, reference, ...)"
    Write-Host "  logs <alias>           Tail real-time logs (local file or Docker)"
    Write-Host "  seed-persons           Seed Natasha Nice & Kendra Lust (scripts/seed-persons.ps1)"
    Write-Host "  reset-seed-persons     Truncate person DB, seed body_types, re-seed persons"
    Write-Host "  seed-persons [args]    Pass -ResetPersonDb via: seed-persons -- -ResetPersonDb"
    Write-Host ""
    Write-Host "If make.exe is blocked by Application Control, use this script instead of make."
}

$TargetKey = $Target.ToLowerInvariant()
switch ($TargetKey) {
    "stop-service" { $TargetKey = "stop" }
    "restart-service" { $TargetKey = "restart" }
}
switch ($TargetKey) {
    "help" { Show-Help }
    "install-lib" {
        & "$RepoRoot\mvnw.cmd" -f sbp-lib-common/pom.xml clean install -DskipTests
    }
    "dbeaver-sync" {
        & "$PSScriptRoot\sync-dbeaver.ps1" -EnvFile $EnvDev
    }
    "migrate-dev" {
        Invoke-Expression "$ComposeDev run --rm --build sbp-migration"
    }
    "migrate-dev-repair" {
        $env:LIQUIBASE_CLEAR_CHECKSUMS = "true"
        try {
            Invoke-Expression "$ComposeDev run --rm --build sbp-migration"
        }
        finally {
            Remove-Item Env:LIQUIBASE_CLEAR_CHECKSUMS -ErrorAction SilentlyContinue
        }
    }
    "migrate-prod" {
        Invoke-Expression "$ComposeProd run --rm --build sbp-migration"
    }
    "infra-dev" {
        Invoke-Expression "$ComposeDev up -d $InfraDev"
        & "$PSScriptRoot\run.ps1" migrate-dev
    }
    "start-dev" {
        Invoke-Expression "$ComposeDev up -d $DbsDev"
        & "$PSScriptRoot\run.ps1" migrate-dev
        Invoke-Expression "$ComposeDev up -d --build"
    }
    "down-dev" {
        Invoke-Expression "$ComposeDev down"
    }
    "down-dev-volumes" {
        Invoke-Expression "$ComposeDev down -v"
    }
    "reset-dev" {
        Invoke-Expression "$ComposeDev down -v"
        & "$PSScriptRoot\run.ps1" infra-dev
    }
    "infra-prod" {
        Invoke-Expression "$ComposeProd up -d $InfraProd"
        & "$PSScriptRoot\run.ps1" migrate-prod
    }
    "start-prod" {
        Invoke-Expression "$ComposeProd up -d $DbsDev"
        & "$PSScriptRoot\run.ps1" migrate-prod
        Invoke-Expression "$ComposeProd up -d --build"
    }
    "down-prod" {
        Invoke-Expression "$ComposeProd down"
    }
    "start" {
        if ([string]::IsNullOrWhiteSpace($ServiceName)) {
            Show-SbpServiceAliases
            exit 1
        }
        $svc = Resolve-SbpService -Name $ServiceName
        if ($Extra -eq "docker") {
            Start-SbpServiceDocker -RepoRoot $RepoRoot -EnvFile $EnvDev -ComposeDev $ComposeDev -Service $svc
        }
        else {
            Start-SbpServiceLocal -RepoRoot $RepoRoot -Service $svc -EnvFile $EnvDev -ComposeDev $ComposeDev
        }
    }
    "logs" {
        if ([string]::IsNullOrWhiteSpace($ServiceName)) {
            Show-SbpServiceAliases
            exit 1
        }
        $svc = Resolve-SbpService -Name $ServiceName
        Tail-SbpServiceLogs -RepoRoot $RepoRoot -EnvFile $EnvDev -ComposeDev $ComposeDev -Service $svc
    }
    "stop" {
        if ([string]::IsNullOrWhiteSpace($ServiceName)) {
            Show-SbpServiceAliases
            exit 1
        }
        $svc = Resolve-SbpService -Name $ServiceName
        Stop-SbpServiceLocal -RepoRoot $RepoRoot -Service $svc -EnvFile $EnvDev
        Stop-SbpServiceDocker -ComposeDev $ComposeDev -Service $svc
    }
    "restart" {
        if ([string]::IsNullOrWhiteSpace($ServiceName)) {
            Show-SbpServiceAliases
            exit 1
        }
        $svc = Resolve-SbpService -Name $ServiceName
        $useDocker = ($Extra -eq "docker")
        Restart-SbpService -RepoRoot $RepoRoot -EnvFile $EnvDev -ComposeDev $ComposeDev -Service $svc -Docker:$useDocker
    }
    "svc-info" {
        Show-SbpServiceInfo -RepoRoot $RepoRoot -EnvFile $EnvDev -ComposeDev $ComposeDev
    }
    "svc-list" {
        Show-SbpServiceAliases
    }
    "seed-persons" {
        & "$PSScriptRoot\seed-persons.ps1" @RemainingArgs
    }
    "reset-seed-persons" {
        & "$PSScriptRoot\seed-persons.ps1" -ResetPersonDb @RemainingArgs
    }
    default {
        Write-Host "Unknown target: $Target"
        Show-Help
        exit 1
    }
}
