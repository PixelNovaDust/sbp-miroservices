# Service catalog and local/Docker runtime helpers (dot-sourced from run.ps1)

function Get-SbpServiceCatalog {
    return @{
        "auth-service"       = @{ Key = "auth"; Module = "sbp-auth-services"; Compose = "sbp-auth-service"; PortEnv = "AUTH_PORT"; Label = "Auth" }
        "auth"               = @{ Key = "auth"; Module = "sbp-auth-services"; Compose = "sbp-auth-service"; PortEnv = "AUTH_PORT"; Label = "Auth" }
        "user-service"       = @{ Key = "user"; Module = "sbp-user-services"; Compose = "sbp-user-service"; PortEnv = "USER_PORT"; Label = "User" }
        "user"               = @{ Key = "user"; Module = "sbp-user-services"; Compose = "sbp-user-service"; PortEnv = "USER_PORT"; Label = "User" }
        "users"              = @{ Key = "user"; Module = "sbp-user-services"; Compose = "sbp-user-service"; PortEnv = "USER_PORT"; Label = "User" }
        "gateway"            = @{ Key = "gateway"; Module = "sbp-api-gateway"; Compose = "sbp-api-gateway"; PortEnv = "GATEWAY_PORT"; Label = "API Gateway" }
        "api-gateway"        = @{ Key = "gateway"; Module = "sbp-api-gateway"; Compose = "sbp-api-gateway"; PortEnv = "GATEWAY_PORT"; Label = "API Gateway" }
        "discovery"          = @{ Key = "discovery"; Module = "sbp-discovery-server"; Compose = "sbp-discovery-server"; PortEnv = "DISCOVERY_PORT"; Label = "Discovery" }
        "eureka"             = @{ Key = "discovery"; Module = "sbp-discovery-server"; Compose = "sbp-discovery-server"; PortEnv = "DISCOVERY_PORT"; Label = "Discovery" }
        "admin-service"      = @{ Key = "admin"; Module = "sbp-admin-service"; Compose = "sbp-admin-service"; PortEnv = "ADMIN_PORT"; Label = "Admin" }
        "admin"              = @{ Key = "admin"; Module = "sbp-admin-service"; Compose = "sbp-admin-service"; PortEnv = "ADMIN_PORT"; Label = "Admin" }
        "person-service"     = @{ Key = "person"; Module = "sbp-person-service"; Compose = "sbp-person-service"; PortEnv = "PERSON_PORT"; Label = "Person" }
        "person"             = @{ Key = "person"; Module = "sbp-person-service"; Compose = "sbp-person-service"; PortEnv = "PERSON_PORT"; Label = "Person" }
        "studio-service"     = @{ Key = "studio"; Module = "sbp-studio-service"; Compose = "sbp-studio-service"; PortEnv = "STUDIO_PORT"; Label = "Studio" }
        "studio"             = @{ Key = "studio"; Module = "sbp-studio-service"; Compose = "sbp-studio-service"; PortEnv = "STUDIO_PORT"; Label = "Studio" }
        "content-service"    = @{ Key = "content"; Module = "sbp-content-service"; Compose = "sbp-content-service"; PortEnv = "CONTENT_PORT"; Label = "Content" }
        "content"            = @{ Key = "content"; Module = "sbp-content-service"; Compose = "sbp-content-service"; PortEnv = "CONTENT_PORT"; Label = "Content" }
        "reference-service"  = @{ Key = "reference"; Module = "sbp-reference-service"; Compose = "sbp-reference-service"; PortEnv = "REFERENCE_PORT"; Label = "Reference" }
        "reference"          = @{ Key = "reference"; Module = "sbp-reference-service"; Compose = "sbp-reference-service"; PortEnv = "REFERENCE_PORT"; Label = "Reference" }
    }
}

function Resolve-SbpService {
    param([string]$Name)
    if ([string]::IsNullOrWhiteSpace($Name)) {
        throw "Service name is required."
    }
    $catalog = Get-SbpServiceCatalog
    $key = $Name.Trim().ToLowerInvariant()
    if (-not $catalog.ContainsKey($key)) {
        $aliases = ($catalog.Keys | Sort-Object -Unique) -join ", "
        throw "Unknown service '$Name'. Try: $aliases"
    }
    return $catalog[$key]
}

function Resolve-SbpGatewayBaseUrl {
    param([string]$Candidate)
    if (-not [string]::IsNullOrWhiteSpace($Candidate)) {
        $normalized = $Candidate.Trim().TrimEnd('/')
        if ($normalized -match '^https?://[^\s/]+') {
            return $normalized
        }
    }
    foreach ($key in @('GATEWAY_SERVICE_URL', 'API_GATEWAY_URL', 'GATEWAY_BASE_URL', 'GATEWAY_URL')) {
        $fromEnv = [Environment]::GetEnvironmentVariable($key)
        if (-not [string]::IsNullOrWhiteSpace($fromEnv)) {
            $normalized = $fromEnv.Trim().TrimEnd('/')
            if ($normalized -match '^https?://[^\s/]+') {
                return $normalized
            }
        }
    }
    $port = $env:GATEWAY_PORT
    if ([string]::IsNullOrWhiteSpace($port)) { $port = '8080' }
    return "http://localhost:$($port.Trim())"
}

function Import-SbpDevEnv {
    param([string]$EnvFilePath, [string]$RepoRoot)
    if (-not (Test-Path -LiteralPath $EnvFilePath)) {
        throw "Missing $EnvFilePath"
    }
    Get-Content -LiteralPath $EnvFilePath | ForEach-Object {
        $line = $_.Trim()
        if ($line.Length -eq 0 -or $line.StartsWith("#")) { return }
        $eq = $line.IndexOf("=")
        if ($eq -lt 1) { return }
        $name = $line.Substring(0, $eq).Trim()
        $val = $line.Substring($eq + 1).Trim()
        if ($val.Length -ge 2 -and $val.StartsWith('"') -and $val.EndsWith('"')) {
            $val = $val.Substring(1, $val.Length - 2)
        }
        Set-Item -Path "env:$name" -Value $val
    }
    Set-Item -Path env:SPRING_PROFILES_ACTIVE -Value "dev" -ErrorAction SilentlyContinue
    if (-not $env:APP_TIMEZONE) { Set-Item env:APP_TIMEZONE "Asia/Kolkata" }
}

function Get-SbpRuntimeDir {
    param([string]$RepoRoot)
    $dir = Join-Path $RepoRoot "scripts\.runtime"
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
    return $dir
}

function Get-SbpRuntimeStatePath {
    param([string]$RepoRoot, [string]$ServiceKey)
    return Join-Path (Get-SbpRuntimeDir $RepoRoot) "$ServiceKey.json"
}

function Get-SbpRuntimeLogPath {
    param([string]$RepoRoot, [string]$ServiceKey)
    return Join-Path (Get-SbpRuntimeDir $RepoRoot) "$ServiceKey.log"
}

function Get-SbpRuntimeErrLogPath {
    param([string]$RepoRoot, [string]$ServiceKey)
    return Join-Path (Get-SbpRuntimeDir $RepoRoot) "$ServiceKey.err.log"
}

function Write-SbpStartupLogTail {
    param(
        [string]$LogPath,
        [string]$ErrLogPath,
        [int]$TailLines = 30
    )
    $shown = $false
    foreach ($pair in @(@{ Path = $LogPath; Label = "stdout" }, @{ Path = $ErrLogPath; Label = "stderr" })) {
        if (Test-Path -LiteralPath $pair.Path) {
            $shown = $true
            Write-Host "--- Last $TailLines lines ($($pair.Label)): $($pair.Path) ---"
            Get-Content -LiteralPath $pair.Path -Tail $TailLines -ErrorAction SilentlyContinue
        }
    }
    if (-not $shown) {
        Write-Host "No log files found yet. Use: .\scripts\run.ps1 logs <alias>  (note the leading dot and backslash)"
    }
}

function Get-SbpCatalogEntryByKey {
    param([string]$ServiceKey)
    $catalog = Get-SbpServiceCatalog
    foreach ($entry in $catalog.GetEnumerator()) {
        if ($entry.Value.Key -eq $ServiceKey) {
            return $entry.Value
        }
    }
    throw "No catalog entry for key $ServiceKey"
}

function Read-SbpRuntimeState {
    param([string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) { return $null }
    return Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json
}

function Write-SbpRuntimeState {
    param([string]$Path, [object]$State)
    $State | ConvertTo-Json | Set-Content -LiteralPath $Path -Encoding UTF8
}

function Remove-SbpRuntimeState {
    param([string]$Path)
    if (Test-Path -LiteralPath $Path) {
        Remove-Item -LiteralPath $Path -Force
    }
}

function Get-SbpProcessAlive {
    param([int]$ProcessId)
    if ($ProcessId -le 0) { return $false }
    $p = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    return $null -ne $p
}

function Get-SbpPortOwnerPid {
    param([int]$Port)
    if ($Port -le 0) { return $null }
    $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -eq $conn) { return $null }
    return [int]$conn.OwningProcess
}

function Stop-SbpProcessTree {
    param([int]$ProcessId)
    if ($ProcessId -le 0) { return }
    if (-not (Get-SbpProcessAlive -ProcessId $ProcessId)) { return }
    & taskkill.exe /PID $ProcessId /T /F 2>$null | Out-Null
}

function Stop-SbpJavaListenerOnPort {
    param(
        [int]$Port,
        [string]$Label
    )
    if ($Port -le 0) { return $false }
    $portPid = Get-SbpPortOwnerPid -Port $Port
    if ($null -eq $portPid) { return $false }
    $proc = Get-Process -Id $portPid -ErrorAction SilentlyContinue
    if ($null -eq $proc) { return $false }
    $isJava = ($proc.ProcessName -like "*java*") -or ($proc.Path -like "*java*")
    if (-not $isJava) {
        Write-Warning "$Label port $Port is held by non-JVM PID $portPid ($($proc.ProcessName)); not killing."
        return $false
    }
    Stop-SbpProcessTree -ProcessId $portPid
    Write-Host "Stopped JVM on port $Port (PID $portPid)."
    return $true
}

function Get-SbpServicePort {
    param(
        [hashtable]$Service,
        [string]$EnvFilePath
    )
    $port = [int](Get-Item "env:$($Service.PortEnv)" -ErrorAction SilentlyContinue).Value
    if ($port -le 0) {
        throw "Port env $($Service.PortEnv) is not set in $EnvFilePath"
    }
    return $port
}

function Test-SbpServiceRunning {
    param(
        [string]$RepoRoot,
        [string]$EnvFile,
        [string]$ComposeDev,
        [hashtable]$Service
    )
    Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot $EnvFile) -RepoRoot $RepoRoot
    $port = Get-SbpServicePort -Service $Service -EnvFilePath (Join-Path $RepoRoot $EnvFile)

    $statePath = Get-SbpRuntimeStatePath -RepoRoot $RepoRoot -ServiceKey $Service.Key
    $state = Read-SbpRuntimeState -Path $statePath
    if ($null -ne $state -and (Get-SbpProcessAlive -ProcessId ([int]$state.pid))) {
        return $true
    }
    if ($null -ne (Get-SbpPortOwnerPid -Port $port)) {
        return $true
    }
    $docker = Get-SbpDockerServiceRow -ComposeCmd $ComposeDev -ComposeServiceName $Service.Compose
    if ($null -ne $docker -and $docker.State -match "running") {
        return $true
    }
    return $false
}

function Wait-SbpPortReady {
    param(
        [int]$Port,
        [string]$Label,
        [int]$TimeoutSec = 120
    )
    if ($Port -le 0) { return $true }
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        if ($null -ne (Get-SbpPortOwnerPid -Port $Port)) {
            Write-Host "$Label is listening on port $Port."
            return $true
        }
        Start-Sleep -Seconds 2
    }
    Write-Warning "$Label did not bind port $Port within ${TimeoutSec}s."
    return $false
}

function Ensure-SbpDependenciesLocal {
    param(
        [string]$RepoRoot,
        [string]$EnvFile,
        [string]$ComposeDev,
        [hashtable]$Service
    )
    $discovery = Get-SbpCatalogEntryByKey -ServiceKey "discovery"
    if ($Service.Key -ne "discovery") {
        if (-not (Test-SbpServiceRunning -RepoRoot $RepoRoot -EnvFile $EnvFile -ComposeDev $ComposeDev -Service $discovery)) {
            Write-Host "Dependency: starting Discovery (Eureka) first..."
            Start-SbpServiceLocal -RepoRoot $RepoRoot -Service $discovery -EnvFile $EnvFile -ComposeDev $ComposeDev -SkipDependencies
            Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot $EnvFile) -RepoRoot $RepoRoot
            Wait-SbpPortReady -Port (Get-SbpServicePort -Service $discovery -EnvFilePath (Join-Path $RepoRoot $EnvFile)) -Label "Discovery"
        }
    }

    if ($Service.Key -ne "discovery" -and $Service.Key -ne "gateway") {
        $gateway = Get-SbpCatalogEntryByKey -ServiceKey "gateway"
        if (-not (Test-SbpServiceRunning -RepoRoot $RepoRoot -EnvFile $EnvFile -ComposeDev $ComposeDev -Service $gateway)) {
            Write-Host "Dependency: starting API Gateway..."
            Start-SbpServiceLocal -RepoRoot $RepoRoot -Service $gateway -EnvFile $EnvFile -ComposeDev $ComposeDev -SkipDependencies
            Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot $EnvFile) -RepoRoot $RepoRoot
            Wait-SbpPortReady -Port (Get-SbpServicePort -Service $gateway -EnvFilePath (Join-Path $RepoRoot $EnvFile)) -Label "API Gateway"
        }
    }
    elseif ($Service.Key -eq "gateway") {
        if (-not (Test-SbpServiceRunning -RepoRoot $RepoRoot -EnvFile $EnvFile -ComposeDev $ComposeDev -Service $discovery)) {
            Write-Host "Dependency: starting Discovery (Eureka) first..."
            Start-SbpServiceLocal -RepoRoot $RepoRoot -Service $discovery -EnvFile $EnvFile -ComposeDev $ComposeDev -SkipDependencies
            Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot $EnvFile) -RepoRoot $RepoRoot
            Wait-SbpPortReady -Port (Get-SbpServicePort -Service $discovery -EnvFilePath (Join-Path $RepoRoot $EnvFile)) -Label "Discovery"
        }
    }
}

function Ensure-SbpDependenciesDocker {
    param(
        [string]$RepoRoot,
        [string]$EnvFile,
        [string]$ComposeDev,
        [hashtable]$Service
    )
    $discovery = Get-SbpCatalogEntryByKey -ServiceKey "discovery"
    if ($Service.Key -ne "discovery") {
        if (-not (Test-SbpServiceRunning -RepoRoot $RepoRoot -EnvFile $EnvFile -ComposeDev $ComposeDev -Service $discovery)) {
            Write-Host "Dependency: starting Discovery (Docker)..."
            Invoke-Expression "$ComposeDev up -d --build $($discovery.Compose)"
            Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot $EnvFile) -RepoRoot $RepoRoot
            Wait-SbpPortReady -Port (Get-SbpServicePort -Service $discovery -EnvFilePath (Join-Path $RepoRoot $EnvFile)) -Label "Discovery"
        }
    }
    if ($Service.Key -ne "discovery" -and $Service.Key -ne "gateway") {
        $gateway = Get-SbpCatalogEntryByKey -ServiceKey "gateway"
        if (-not (Test-SbpServiceRunning -RepoRoot $RepoRoot -EnvFile $EnvFile -ComposeDev $ComposeDev -Service $gateway)) {
            Write-Host "Dependency: starting API Gateway (Docker)..."
            Invoke-Expression "$ComposeDev up -d --build $($gateway.Compose)"
            Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot $EnvFile) -RepoRoot $RepoRoot
            Wait-SbpPortReady -Port (Get-SbpServicePort -Service $gateway -EnvFilePath (Join-Path $RepoRoot $EnvFile)) -Label "API Gateway"
        }
    }
    elseif ($Service.Key -eq "gateway") {
        if (-not (Test-SbpServiceRunning -RepoRoot $RepoRoot -EnvFile $EnvFile -ComposeDev $ComposeDev -Service $discovery)) {
            Write-Host "Dependency: starting Discovery (Docker)..."
            Invoke-Expression "$ComposeDev up -d --build $($discovery.Compose)"
            Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot $EnvFile) -RepoRoot $RepoRoot
            Wait-SbpPortReady -Port (Get-SbpServicePort -Service $discovery -EnvFilePath (Join-Path $RepoRoot $EnvFile)) -Label "Discovery"
        }
    }
}

function Get-SbpDockerServiceRow {
    param([string]$ComposeCmd, [string]$ComposeServiceName)
    if ([string]::IsNullOrWhiteSpace($ComposeCmd)) { return $null }
    $json = Invoke-Expression "$ComposeCmd ps --format json $ComposeServiceName 2>`$null" | Out-String
    if ([string]::IsNullOrWhiteSpace($json)) { return $null }
    $lines = $json.Trim() -split "`n" | Where-Object { $_.Trim().Length -gt 0 }
    foreach ($line in $lines) {
        try {
            $o = $line | ConvertFrom-Json
            if ($o.Service -eq $ComposeServiceName) { return $o }
        }
        catch { }
    }
    return $null
}

function Start-SbpServiceLocal {
    param(
        [string]$RepoRoot,
        [hashtable]$Service,
        [string]$EnvFile,
        [string]$ComposeDev = "",
        [switch]$SkipDependencies
    )
    if (-not $SkipDependencies -and -not [string]::IsNullOrWhiteSpace($ComposeDev)) {
        Ensure-SbpDependenciesLocal -RepoRoot $RepoRoot -EnvFile $EnvFile -ComposeDev $ComposeDev -Service $Service
    }

    $statePath = Get-SbpRuntimeStatePath -RepoRoot $RepoRoot -ServiceKey $Service.Key
    $logPath = Get-SbpRuntimeLogPath -RepoRoot $RepoRoot -ServiceKey $Service.Key
    $existing = Read-SbpRuntimeState -Path $statePath
    if ($null -ne $existing -and (Get-SbpProcessAlive -ProcessId ([int]$existing.pid))) {
        Write-Host "Already running (local PID $($existing.pid)). Use 'stop $($Service.Key)' first."
        return
    }
    if (Test-SbpServiceRunning -RepoRoot $RepoRoot -EnvFile $EnvFile -ComposeDev $ComposeDev -Service $Service) {
        Write-Host "$($Service.Label) already running (port or Docker)."
        return
    }

    Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot $EnvFile) -RepoRoot $RepoRoot
    $port = Get-SbpServicePort -Service $Service -EnvFilePath (Join-Path $RepoRoot $EnvFile)

    $portPid = Get-SbpPortOwnerPid -Port $port
    if ($null -ne $portPid) {
        throw "Port $port is already in use (PID $portPid). Stop that process or change $($Service.PortEnv) in $EnvFile."
    }

    $mvnw = Join-Path $RepoRoot "mvnw.cmd"
    if (-not (Test-Path -LiteralPath $mvnw)) {
        throw "Missing $mvnw"
    }
    $pom = Join-Path $RepoRoot "$($Service.Module)/pom.xml"
    if (-not (Test-Path -LiteralPath $pom)) {
        throw "Missing module POM: $pom"
    }
    $jvmTz = $env:APP_TIMEZONE
    if ([string]::IsNullOrWhiteSpace($jvmTz)) { $jvmTz = "Asia/Kolkata" }

    $errLogPath = Get-SbpRuntimeErrLogPath -RepoRoot $RepoRoot -ServiceKey $Service.Key
    New-Item -ItemType File -Path $logPath -Force | Out-Null
    New-Item -ItemType File -Path $errLogPath -Force | Out-Null

    $mvnArgs = @(
        "-f", $pom,
        "spring-boot:run",
        "-Dspring-boot.run.profiles=dev",
        "-Dspring-boot.run.jvmArguments=-Duser.timezone=$jvmTz"
    )

    Write-Host "Starting $($Service.Label) in background (port $port, log: $logPath)..."
    $proc = Start-Process -FilePath $mvnw -ArgumentList $mvnArgs -WorkingDirectory $RepoRoot -PassThru -WindowStyle Hidden `
        -RedirectStandardOutput $logPath -RedirectStandardError $errLogPath

    $state = [ordered]@{
        mode      = "local"
        key       = $Service.Key
        module    = $Service.Module
        compose   = $Service.Compose
        port      = $port
        pid       = $proc.Id
        logFile   = $logPath
        errLogFile = $errLogPath
        startedAt = (Get-Date).ToUniversalTime().ToString("o")
    }
    Write-SbpRuntimeState -Path $statePath -State $state
    Write-Host "Started $($Service.Label) - Maven wrapper PID $($proc.Id), HTTP port $port"
    Write-Host "Tail logs: .\scripts\run.ps1 logs $($Service.Key)"

    $ready = Wait-SbpPortReady -Port $port -Label $Service.Label -TimeoutSec 180
    $wrapperAlive = Get-SbpProcessAlive -ProcessId $proc.Id

    if (-not $ready) {
        if (-not $wrapperAlive) {
            Write-Host "Maven process exited before the app listened on port $port."
        }
        else {
            Write-Host "Still starting (Maven compile can take several minutes on first run). Tail logs in another window:"
        }
        Write-SbpStartupLogTail -LogPath $logPath -ErrLogPath $errLogPath
    }
}

function Start-SbpServiceDocker {
    param(
        [string]$RepoRoot,
        [string]$EnvFile,
        [string]$ComposeDev,
        [hashtable]$Service
    )
    Ensure-SbpDependenciesDocker -RepoRoot $RepoRoot -EnvFile $EnvFile -ComposeDev $ComposeDev -Service $Service
    Write-Host "Starting $($Service.Label) via Docker dev compose..."
    Invoke-Expression "$ComposeDev up -d --build $($Service.Compose)"
    Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot $EnvFile) -RepoRoot $RepoRoot
    $port = Get-SbpServicePort -Service $Service -EnvFilePath (Join-Path $RepoRoot $EnvFile)
    Wait-SbpPortReady -Port $port -Label $Service.Label -TimeoutSec 120
    Write-Host "Docker logs: .\scripts\run.ps1 logs $($Service.Key)"
}

function Stop-SbpServiceLocal {
    param(
        [string]$RepoRoot,
        [hashtable]$Service,
        [string]$EnvFile
    )
    $statePath = Get-SbpRuntimeStatePath -RepoRoot $RepoRoot -ServiceKey $Service.Key
    $state = Read-SbpRuntimeState -Path $statePath
    $stopped = $false
    $wrapperPid = $null

    if ($null -ne $state -and (Get-SbpProcessAlive -ProcessId ([int]$state.pid))) {
        $wrapperPid = [int]$state.pid
        Stop-SbpProcessTree -ProcessId $wrapperPid
        Write-Host "Stopped local $($Service.Label) (PID $wrapperPid, process tree)."
        $stopped = $true
    }

    Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot $EnvFile) -RepoRoot $RepoRoot
    $port = [int](Get-Item "env:$($Service.PortEnv)" -ErrorAction SilentlyContinue).Value
    if (Stop-SbpJavaListenerOnPort -Port $port -Label $Service.Label) {
        $stopped = $true
    }

    Remove-SbpRuntimeState -Path $statePath
    if (-not $stopped) {
        Write-Host "No local $($Service.Label) process found (checked state file and port $port)."
    }
}

function Stop-SbpServiceDocker {
    param(
        [string]$ComposeDev,
        [hashtable]$Service
    )
    Invoke-Expression "$ComposeDev stop $($Service.Compose)"
    Write-Host "Stopped Docker container $($Service.Compose)."
}

function Wait-SbpPortFree {
    param(
        [int]$Port,
        [string]$Label,
        [int]$TimeoutSec = 45
    )
    if ($Port -le 0) { return }
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        if ($null -eq (Get-SbpPortOwnerPid -Port $Port)) {
            return
        }
        Start-Sleep -Seconds 1
    }
    $owner = Get-SbpPortOwnerPid -Port $Port
    if ($null -ne $owner) {
        Write-Warning "$Label port $Port still in use (PID $owner) after ${TimeoutSec}s."
    }
}

function Restart-SbpService {
    param(
        [string]$RepoRoot,
        [string]$EnvFile,
        [string]$ComposeDev,
        [hashtable]$Service,
        [switch]$Docker
    )
    Write-Host "Restarting $($Service.Label)..."
    Stop-SbpServiceLocal -RepoRoot $RepoRoot -Service $Service -EnvFile $EnvFile
    Stop-SbpServiceDocker -ComposeDev $ComposeDev -Service $Service
    Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot $EnvFile) -RepoRoot $RepoRoot
    $port = Get-SbpServicePort -Service $Service -EnvFilePath (Join-Path $RepoRoot $EnvFile)
    Wait-SbpPortFree -Port $port -Label $Service.Label
    if ($null -ne (Get-SbpPortOwnerPid -Port $port)) {
        [void](Stop-SbpJavaListenerOnPort -Port $port -Label $Service.Label)
        Wait-SbpPortFree -Port $port -Label $Service.Label -TimeoutSec 15
    }
    if ($Docker) {
        Start-SbpServiceDocker -RepoRoot $RepoRoot -EnvFile $EnvFile -ComposeDev $ComposeDev -Service $Service
    }
    else {
        Start-SbpServiceLocal -RepoRoot $RepoRoot -Service $Service -EnvFile $EnvFile -ComposeDev $ComposeDev
    }
}


function Show-SbpServiceInfo {
    param(
        [string]$RepoRoot,
        [string]$EnvFile,
        [string]$ComposeDev
    )
    Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot $EnvFile) -RepoRoot $RepoRoot

    $seen = @{}
    $rows = @()
    $catalog = Get-SbpServiceCatalog
    foreach ($entry in $catalog.GetEnumerator()) {
        $svc = $entry.Value
        if ($seen.ContainsKey($svc.Key)) { continue }
        $seen[$svc.Key] = $true

        $port = [int](Get-Item "env:$($svc.PortEnv)" -ErrorAction SilentlyContinue).Value
        $statePath = Get-SbpRuntimeStatePath -RepoRoot $RepoRoot -ServiceKey $svc.Key
        $state = Read-SbpRuntimeState -Path $statePath

        $status = "stopped"
        $procId = "-"
        $since = "-"
        $mode = "-"

        if ($null -ne $state -and (Get-SbpProcessAlive -ProcessId ([int]$state.pid))) {
            $status = "running"
            $procId = "$($state.pid)"
            $mode = "local"
            try {
                $started = [DateTime]::Parse($state.startedAt).ToLocalTime()
                $since = (New-TimeSpan -Start $started -End (Get-Date)).ToString()
            }
            catch {
                $since = $state.startedAt
            }
        }
        else {
            $portPid = Get-SbpPortOwnerPid -Port $port
            if ($null -ne $portPid) {
                $status = "running"
                $procId = "$portPid"
                $mode = "local (port)"
                $p = Get-Process -Id $portPid -ErrorAction SilentlyContinue
                if ($p) { $since = (New-TimeSpan -Start $p.StartTime -End (Get-Date)).ToString() }
            }
        }

        $docker = Get-SbpDockerServiceRow -ComposeCmd $ComposeDev -ComposeServiceName $svc.Compose
        if ($null -ne $docker -and $docker.State -match "running") {
            if ($status -eq "stopped") {
                $status = "running"
                $mode = "docker"
                $procId = $docker.ID.Substring(0, [Math]::Min(12, $docker.ID.Length))
                $since = $docker.Status
            }
            else {
                $mode = "$mode + docker"
            }
        }

        $rows += [PSCustomObject]@{
            Service = $svc.Label
            Alias   = $svc.Key
            Port    = $port
            PID     = $procId
            Uptime  = $since
            Status  = $status
            Mode    = $mode
        }
    }

    $rows | Sort-Object Service | Format-Table -AutoSize
    Write-Host "Local starts use $EnvFile. State files: scripts/.runtime/*.json"
    Write-Host 'Start: .\scripts\run.ps1 start auth-service  |  Docker: .\scripts\run.ps1 start auth-service docker'
    Write-Host 'Logs:  .\scripts\run.ps1 logs auth-service'
}

function Tail-SbpServiceLogs {
    param(
        [string]$RepoRoot,
        [string]$EnvFile,
        [string]$ComposeDev,
        [hashtable]$Service
    )
    $logPath = Get-SbpRuntimeLogPath -RepoRoot $RepoRoot -ServiceKey $Service.Key
    $statePath = Get-SbpRuntimeStatePath -RepoRoot $RepoRoot -ServiceKey $Service.Key
    $state = Read-SbpRuntimeState -Path $statePath

    $docker = Get-SbpDockerServiceRow -ComposeCmd $ComposeDev -ComposeServiceName $Service.Compose
    $dockerRunning = ($null -ne $docker -and $docker.State -match "running")

    if ($dockerRunning -and ($null -eq $state -or $state.mode -eq "docker" -or -not (Get-SbpProcessAlive -ProcessId ([int]$state.pid)))) {
        Write-Host "Tailing Docker logs for $($Service.Compose) (Ctrl+C to stop)..."
        Invoke-Expression "$ComposeDev logs -f --tail 100 $($Service.Compose)"
        return
    }

    $errLogPath = Get-SbpRuntimeErrLogPath -RepoRoot $RepoRoot -ServiceKey $Service.Key
    if ($null -ne $state -and $state.PSObject.Properties.Name -contains "errLogFile") {
        $errLogPath = [string]$state.errLogFile
    }

    if (-not (Test-Path -LiteralPath $logPath)) {
        if ($dockerRunning) {
            Write-Host "No local log file; using Docker logs..."
            Invoke-Expression "$ComposeDev logs -f --tail 100 $($Service.Compose)"
            return
        }
        throw "Log file not found: $logPath. Start the service first with: .\scripts\run.ps1 start $($Service.Key)"
    }

    Write-Host "Tailing $logPath (Ctrl+C to stop)..."
    if (Test-Path -LiteralPath $errLogPath) {
        Write-Host "stderr file: $errLogPath (shown first if non-empty, then stdout stream)"
        $errSize = (Get-Item -LiteralPath $errLogPath).Length
        if ($errSize -gt 0) {
            Get-Content -LiteralPath $errLogPath -Tail 40
            Write-Host "--- stdout (live) ---"
        }
    }
    Get-Content -LiteralPath $logPath -Tail 80 -Wait
}

function Show-SbpServiceAliases {
    $catalog = Get-SbpServiceCatalog
    $unique = @{}
    foreach ($entry in $catalog.GetEnumerator()) {
        $unique[$entry.Value.Key] = $entry.Value.Label
    }
    Write-Host "Service aliases (use with start / stop):"
    foreach ($k in ($unique.Keys | Sort-Object)) {
        Write-Host "  $k - $($unique[$k])"
    }
}
