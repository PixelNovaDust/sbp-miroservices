# SBP stack commands for Windows (use when make.exe is blocked or unavailable)
param(
    [Parameter(Position = 0, ValueFromRemainingArguments = $true)]
    [string[]]$ArgsIn = @("help")
)

$ErrorActionPreference = "Stop"
$ScriptsDir = $PSScriptRoot
$RepoRoot = Split-Path -Parent $ScriptsDir
Set-Location $RepoRoot
. "$ScriptsDir\lib\service-runtime.ps1"

$EnvDev = "env/.env.dev"
$EnvProd = "env/.env.prod"
$ComposeDev = "docker compose -p sbp-dev --env-file $EnvDev -f docker/docker-compose.dev.yaml"
$ComposeProd = "docker compose -p sbp-prod --env-file $EnvProd -f docker/docker-compose.prod.yaml"
$InfraDev = "auth-db user-db person-db studio-db content-db reference-db file-storage-db prometheus grafana"
$InfraProd = $InfraDev
$DbsDev = "auth-db user-db person-db studio-db content-db reference-db file-storage-db"

function Show-HelpRow {
    param([string]$Cmd, [string]$Desc)
    $pad = $Cmd.PadRight(36)
    Write-Host "  " -NoNewline
    Write-Host $pad -ForegroundColor Cyan -NoNewline
    Write-Host " " -NoNewline
    Write-Host $Desc -ForegroundColor Yellow
}

function Show-Help {
    Show-HelpRow "help" "Print this command list"
    Show-HelpRow "lib" "Install the shared library into ~/.m2 for Maven and the IDE"
    Show-HelpRow "dbeaver" "Update DBeaver connections from env/.env.dev (after env changes)"
    Write-Host ""
    Show-HelpRow "up dev" "Start dev databases, migrate, then build and run all services"
    Show-HelpRow "up dev -Infra" "Databases and monitoring only — handy when apps run in the IDE"
    Show-HelpRow "up prod" "Start prod databases, migrate, then the full production stack"
    Show-HelpRow "up prod -Infra" "Production databases and monitoring, with migrations"
    Write-Host ""
    Show-HelpRow "down dev" "Stop the dev stack but keep database data on disk"
    Show-HelpRow "down dev -Volumes" "Stop dev and delete database volumes (all local DB data gone)"
    Show-HelpRow "down prod" "Stop the production compose project"
    Write-Host ""
    Show-HelpRow "reset dev" "Fresh dev databases: wipe volumes, then infra + migrate again"
    Write-Host ""
    Show-HelpRow "migrate dev" "Apply schema changes (dev Postgres containers must be up)"
    Show-HelpRow "migrate dev -Repair" "Clear a Liquibase checksum mismatch, then migrate (dev only)"
    Show-HelpRow "migrate prod" "Apply schema changes on production databases"
    Write-Host ""
    Show-HelpRow "seed dev" "Load sample taxonomies, studios, performers, and titles via the APIs"
    Show-HelpRow "seed dev -Migrate" "Run migrations first, then load the dev sample catalog"
    Show-HelpRow "seed dev -Reset" "Remove previous seed rows, then load the catalog again"
    Show-HelpRow "seed dev -ResetDb" "Empty the person database, then run a full seed"
    Show-HelpRow "seed dev -Force" "Refresh links and extras for performers already in the catalog"
    Write-Host ""
    Show-HelpRow "svc list" "Show service short names (auth, gateway, person, …)"
    Show-HelpRow "svc status" "See what is running, on which ports, locally or in Docker"
    Show-HelpRow "svc start <alias> [-Docker]" "Run one service on your machine (or via Compose)"
    Show-HelpRow "svc stop <alias>" "Stop that service locally and in Docker if applicable"
    Show-HelpRow "svc restart <alias> [-Docker]" "Stop and start the service again"
    Show-HelpRow "svc logs <alias>" "Follow log output for a service (Ctrl+C to stop)"
    Write-Host ""
    Write-Host "  Scripts: run.ps1 here · sample data in seed/ · helpers in lib/" -ForegroundColor DarkGray
}

function Invoke-Migrate {
    param([string]$EnvName, [switch]$Repair)
    if ($EnvName -eq "dev") {
        if ($Repair) { $env:SBP_CLEAR_LIQUIBASE_CHECKSUMS = "true" }
        try {
            Invoke-Expression "$ComposeDev run --rm --build sbp-migration"
        }
        finally {
            Remove-Item Env:SBP_CLEAR_LIQUIBASE_CHECKSUMS -ErrorAction SilentlyContinue
        }
    }
    elseif ($EnvName -eq "prod") {
        if ($Repair) { throw "migrate prod -Repair is not supported." }
        Invoke-Expression "$ComposeProd run --rm --build sbp-migration"
    }
    else { throw "Usage: migrate dev|prod [-Repair]" }
}

function Invoke-Up {
    param([string]$EnvName, [switch]$Infra)
    if ($EnvName -eq "dev") {
        if ($Infra) {
            Invoke-Expression "$ComposeDev up -d $InfraDev"
            Invoke-Migrate dev
        }
        else {
            Invoke-Expression "$ComposeDev up -d $DbsDev"
            Invoke-Migrate dev
            Invoke-Expression "$ComposeDev up -d --build"
        }
    }
    elseif ($EnvName -eq "prod") {
        if ($Infra) {
            Invoke-Expression "$ComposeProd up -d $InfraProd"
            Invoke-Migrate prod
        }
        else {
            Invoke-Expression "$ComposeProd up -d $DbsDev"
            Invoke-Migrate prod
            Invoke-Expression "$ComposeProd up -d --build"
        }
    }
    else { throw "Usage: up dev|prod [-Infra]" }
}

function Invoke-Down {
    param([string]$EnvName, [switch]$Volumes)
    if ($EnvName -eq "dev") {
        if ($Volumes) { Invoke-Expression "$ComposeDev down -v" }
        else { Invoke-Expression "$ComposeDev down" }
    }
    elseif ($EnvName -eq "prod") { Invoke-Expression "$ComposeProd down" }
    else { throw "Usage: down dev|prod [-Volumes]" }
}

function Invoke-ResetDev {
    Invoke-Down dev -Volumes
    Invoke-Up dev -Infra
}

function Dispatch {
    param([string[]]$Cmd)
    $group = $Cmd[0].ToLowerInvariant()
    $rest = @()
    if ($Cmd.Count -gt 1) { $rest = $Cmd[1..($Cmd.Length - 1)] }

    switch ($group) {
        "help" { Show-Help }
        "lib" { & "$ScriptsDir\lib\install-lib.ps1" }
        "dbeaver" { & "$ScriptsDir\lib\sync-dbeaver.ps1" -EnvFile $EnvDev }
        "migrate" {
            $envName = $rest[0]
            $repair = $rest -contains "-Repair" -or $rest -contains "--repair"
            Invoke-Migrate -EnvName $envName -Repair:$repair
        }
        "up" {
            $envName = $rest[0]
            $infra = $rest -contains "-Infra" -or $rest -contains "--infra"
            Invoke-Up -EnvName $envName -Infra:$infra
        }
        "down" {
            $envName = $rest[0]
            $vol = $rest -contains "-Volumes" -or $rest -contains "--volumes"
            Invoke-Down -EnvName $envName -Volumes:$vol
        }
        "reset" {
            if ($rest[0] -ne "dev") { throw "reset is only supported for dev." }
            Invoke-ResetDev
        }
        "seed" {
            $which = $rest[0]
            if ($which -ne "dev") {
                throw "Usage: seed dev [-Migrate] [-Reset] [-ResetDb] [-Force]"
            }
            $tail = @()
            if ($rest.Count -gt 1) { $tail = $rest[1..($rest.Length - 1)] }
            $migrate = $tail -contains "-Migrate" -or $tail -contains "--migrate"
            $resetDb = $tail -contains "-ResetDb" -or $tail -contains "--reset-db"
            $reset = (-not $resetDb) -and ($tail -contains "-Reset" -or $tail -contains "--reset")
            $force = $tail -contains "-Force" -or $tail -contains "--force"
            if ($migrate) { Invoke-Migrate dev }
            $pyArgs = @()
            if ($resetDb) { $pyArgs += "-ResetDb" }
            elseif ($reset) { $pyArgs += "-Reset" }
            if ($force) { $pyArgs += "-Force" }
            & "$ScriptsDir\seed\seed-dev.ps1" @pyArgs
        }
        "svc" {
            $action = $rest[0]
            $alias = if ($rest.Count -gt 1) { $rest[1] } else { "" }
            $docker = $rest -contains "-Docker" -or $rest -contains "--docker"
            switch ($action) {
                "list" { Show-SbpServiceAliases }
                { $_ -in "status", "info" } { Show-SbpServiceInfo -RepoRoot $RepoRoot -EnvFile $EnvDev -ComposeDev $ComposeDev }
                "start" {
                    if ([string]::IsNullOrWhiteSpace($alias)) { Show-SbpServiceAliases; exit 1 }
                    $svc = Resolve-SbpService -Name $alias
                    if ($docker) { Start-SbpServiceDocker -RepoRoot $RepoRoot -EnvFile $EnvDev -ComposeDev $ComposeDev -Service $svc }
                    else { Start-SbpServiceLocal -RepoRoot $RepoRoot -Service $svc -EnvFile $EnvDev -ComposeDev $ComposeDev }
                }
                "stop" {
                    if ([string]::IsNullOrWhiteSpace($alias)) { Show-SbpServiceAliases; exit 1 }
                    $svc = Resolve-SbpService -Name $alias
                    Stop-SbpServiceLocal -RepoRoot $RepoRoot -Service $svc -EnvFile $EnvDev
                    Stop-SbpServiceDocker -ComposeDev $ComposeDev -Service $svc
                }
                "restart" {
                    if ([string]::IsNullOrWhiteSpace($alias)) { Show-SbpServiceAliases; exit 1 }
                    $svc = Resolve-SbpService -Name $alias
                    Restart-SbpService -RepoRoot $RepoRoot -EnvFile $EnvDev -ComposeDev $ComposeDev -Service $svc -Docker:$docker
                }
                "logs" {
                    if ([string]::IsNullOrWhiteSpace($alias)) { Show-SbpServiceAliases; exit 1 }
                    $svc = Resolve-SbpService -Name $alias
                    Tail-SbpServiceLogs -RepoRoot $RepoRoot -EnvFile $EnvDev -ComposeDev $ComposeDev -Service $svc
                }
                default { throw "Usage: svc list|status|start|stop|restart|logs <alias> [-Docker]" }
            }
        }
        default {
            Write-Host "Unknown command: $group"
            Show-Help
            exit 1
        }
    }
}

if ($null -eq $ArgsIn -or $ArgsIn.Count -eq 0) {
    Dispatch -Cmd @("help")
}
else {
    Dispatch -Cmd $ArgsIn
}
