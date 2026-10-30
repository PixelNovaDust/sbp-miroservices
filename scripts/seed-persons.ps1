# Seed demo persons (Natasha Nice, Kendra Lust) using existing taxonomies, studios, and social platforms.
param(
    [string]$GatewayBase = "http://localhost:8080",
    [string]$Actor = "dev-user",
    [string]$RelationshipTypeSlug = "exclusive",
    [guid]$RelationshipTypeId = [guid]::Empty,
    [switch]$SkipIfExists,
    [switch]$ForceCreate,
    [switch]$ResetPersonDb
)

$ErrorActionPreference = "Stop"
if ($ResetPersonDb) {
    $SkipIfExists = $false
    $ForceCreate = $true
}
elseif (-not $PSBoundParameters.ContainsKey('SkipIfExists') -and -not $ForceCreate) {
    $SkipIfExists = $true
}
$RepoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $RepoRoot
. "$PSScriptRoot\service-runtime.ps1"
Import-SbpDevEnv -EnvFilePath (Join-Path $RepoRoot "env/.env.dev") -RepoRoot $RepoRoot

$GatewayBase = Resolve-SbpGatewayBaseUrl -Candidate $GatewayBase
Write-Host "Gateway: $GatewayBase"

$EnvDev = Join-Path $RepoRoot "env/.env.dev"
$ComposeDev = "docker compose -p sbp-dev --env-file $EnvDev -f docker/docker-compose.dev.yaml"

$PersonsBase = "$GatewayBase/api/v1/persons"
$BodyTypesBase = "$PersonsBase/body-types"
$RelationshipTypesBase = "$PersonsBase/relationship-types"
$TaxonomiesBase = "$GatewayBase/api/v1/taxonomies"
$StudiosBase = "$GatewayBase/api/v1/studios"
$PlatformsBase = "$GatewayBase/api/v1/social-platforms"

function Invoke-SbpJson {
    param(
        [ValidateSet('GET', 'POST', 'PUT', 'DELETE')]
        [string]$Method,
        [string]$Uri,
        [object]$Body,
        [switch]$AllowNotFound
    )
    $params = @{
        Method      = $Method
        Uri         = $Uri
        ContentType = 'application/json'
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 20 -Compress)
    }
    try {
        return Invoke-RestMethod @params
    }
    catch {
        $is404 = $false
        if ($_.Exception.Response) {
            try { $is404 = [int]$_.Exception.Response.StatusCode -eq 404 } catch { }
        }
        if (-not ($AllowNotFound -and $is404)) {
            Write-Warning "$Method $Uri failed: $($_.Exception.Message)"
            if ($_.ErrorDetails.Message) { Write-Host $_.ErrorDetails.Message }
            elseif ($_.Exception.Response) {
                try {
                    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                    $errBody = $reader.ReadToEnd()
                    if ($errBody) { Write-Host $errBody }
                }
                catch { }
            }
        }
        throw
    }
}

function Get-ApiCatalog {
    Write-Host "Loading taxonomies, studios, and social platforms..."
    $tax = (Invoke-SbpJson -Method GET -Uri "${TaxonomiesBase}?active=true").data
    $studios = (Invoke-SbpJson -Method GET -Uri "${StudiosBase}?active=true").data
    $platforms = (Invoke-SbpJson -Method GET -Uri "${PlatformsBase}?active=true").data
    return @{
        Taxonomies = @($tax)
        Studios    = @($studios)
        Platforms  = @($platforms)
    }
}

function Find-BySlug {
    param([array]$Items, [string]$Slug, [string]$Label, [switch]$Quiet)
    if ([string]::IsNullOrWhiteSpace($Slug)) { return $null }
    $match = $Items | Where-Object { $_.slug -eq $Slug } | Select-Object -First 1
    if ($null -eq $match -and -not $Quiet) {
        Write-Warning "$Label slug '$Slug' not found in API catalog (skip or fix slug in script config)."
    }
    return $match
}

function Resolve-TaxonomyIds {
    param([string[]]$Slugs, [array]$Catalog)
    $ids = @()
    foreach ($s in $Slugs) {
        $t = Find-BySlug -Items $Catalog -Slug $s -Label 'Taxonomy'
        if ($null -ne $t) { $ids += $t.id }
    }
    return $ids
}

function Resolve-RoleTaxonomyIds {
    param([string[]]$Slugs, [array]$Catalog)
    $roleCatalog = @($Catalog | Where-Object { $_.type -eq 'ROLE' })
    if ($Slugs -and $Slugs.Count -gt 0) {
        return Resolve-TaxonomyIds -Slugs $Slugs -Catalog $roleCatalog
    }
    return @()
}

function Resolve-StudioIds {
    param([string[]]$Slugs, [array]$Catalog)
    $ids = @()
    foreach ($s in $Slugs) {
        $st = Find-BySlug -Items $Catalog -Slug $s -Label 'Studio'
        if ($null -ne $st) { $ids += $st.id }
    }
    return $ids
}

function Invoke-DockerDbSql {
    param(
        [string]$Service,
        [string]$DbName,
        [string]$DbUser,
        [string]$Sql,
        [switch]$Scalar
    )
    if ([string]::IsNullOrWhiteSpace($DbName) -or [string]::IsNullOrWhiteSpace($DbUser)) {
        throw "Database name/user missing for $Service. Check env/.env.dev"
    }
    $composeFile = Join-Path $RepoRoot "docker/docker-compose.dev.yaml"
    $statusJson = & docker compose -p sbp-dev --env-file $EnvDev -f $composeFile ps --status running --format json $Service 2>$null
    if ([string]::IsNullOrWhiteSpace(($statusJson | Out-String).Trim())) {
        throw "$Service container is not running. Run: .\scripts\run.ps1 infra-dev"
    }
    $psqlArgs = @("-U", $DbUser, "-d", $DbName)
    if ($Scalar) { $psqlArgs += @("-t", "-A") }
    $dockerArgs = @(
        "compose", "-p", "sbp-dev", "--env-file", $EnvDev, "-f", $composeFile,
        "exec", "-T", $Service, "psql"
    ) + $psqlArgs
    $prevEap = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $out = $Sql | & docker @dockerArgs 2>&1
    }
    finally {
        $ErrorActionPreference = $prevEap
    }
    if ($LASTEXITCODE -ne 0) {
        throw "$Service psql failed: $out"
    }
    if (-not $Scalar) { return $out }
    $line = ($out | Where-Object { $_ -is [string] -and $_.Trim().Length -gt 0 } | Select-Object -First 1)
    if ($null -eq $line) { return $null }
    return $line.ToString().Trim()
}

function Invoke-PersonDbExecViaDocker {
    param([string]$Sql)
    Invoke-DockerDbSql -Service "person-db" -DbName $env:PERSON_DB_NAME -DbUser $env:PERSON_DB_USERNAME -Sql $Sql | Out-Null
}

function Invoke-ReferenceDbExecViaDocker {
    param([string]$Sql)
    Invoke-DockerDbSql -Service "reference-db" -DbName $env:REFERENCE_DB_NAME -DbUser $env:REFERENCE_DB_USERNAME -Sql $Sql | Out-Null
}

function Reset-PersonDatabase {
    Write-Host "Truncating person DB tables and PERSON taxonomy links in reference DB..." -ForegroundColor Yellow
    $truncatePerson = @"
SET client_min_messages TO WARNING;
TRUNCATE TABLE
    person_social_accounts,
    person_aliases,
    person_studios,
    person_physical_attributes,
    persons,
    body_types,
    relationship_types
CASCADE;
"@
    Invoke-PersonDbExecViaDocker -Sql $truncatePerson
    $deleteTaxLinks = "DELETE FROM entity_taxonomies WHERE entity_type = 'PERSON';"
    try {
        Invoke-ReferenceDbExecViaDocker -Sql $deleteTaxLinks
    }
    catch {
        Write-Warning "Could not clear reference entity_taxonomies (reference-db down?): $($_.Exception.Message)"
    }
    Write-Host "Person DB truncated." -ForegroundColor Green
}

function Ensure-LookupTypeViaApi {
    param(
        [string]$ResourceBase,
        [string]$Name,
        [string]$Slug,
        [string]$Description
    )
    # POST is idempotent by slug on the person service (returns existing row if slug matches).
    $created = Invoke-SbpJson -Method POST -Uri $ResourceBase -Body @{
        name        = $Name
        slug        = $Slug
        description = $Description
        actor       = $Actor
    }
    Write-Host "  lookup $Slug -> $($created.data.id)"
    return $created.data.id
}

function Seed-PersonLookupTables {
    Write-Host "Seeding body_types and relationship_types via person API..."
    $bodyTypes = @(
        @{ name = 'Slim'; slug = 'slim'; description = 'Slender build' }
        @{ name = 'Athletic'; slug = 'athletic'; description = 'Toned / athletic build' }
        @{ name = 'Average'; slug = 'average'; description = 'Average build' }
        @{ name = 'Curvy'; slug = 'curvy'; description = 'Curvy build' }
        @{ name = 'Petite'; slug = 'petite'; description = 'Petite frame' }
        @{ name = 'Plus Size'; slug = 'plus-size'; description = 'Plus size build' }
    )
    foreach ($bt in $bodyTypes) {
        [void](Ensure-LookupTypeViaApi -ResourceBase $BodyTypesBase -Name $bt.name -Slug $bt.slug -Description $bt.description)
    }
    [void](Ensure-LookupTypeViaApi -ResourceBase $RelationshipTypesBase -Name 'Exclusive' -Slug $RelationshipTypeSlug -Description 'Primary studio contract (seed)')
}

function Get-BodyTypeIdBySlug {
    param([string]$Slug)
    if ([string]::IsNullOrWhiteSpace($Slug)) { return $null }
    $encodedSlug = [Uri]::EscapeDataString($Slug)
    try {
        $resp = Invoke-SbpJson -Method GET -Uri "$BodyTypesBase/by-slug/$encodedSlug" -AllowNotFound
        return $resp.data.id
    }
    catch {
        return Ensure-LookupTypeViaApi -ResourceBase $BodyTypesBase -Name $Slug -Slug $Slug -Description "Auto-created for seed"
    }
}

function Get-RelationshipTypeId {
    param([string]$Slug)
    if ($RelationshipTypeId -ne [guid]::Empty) {
        Write-Host "Using relationship type id from -RelationshipTypeId parameter."
        return $RelationshipTypeId.ToString()
    }
    return Ensure-LookupTypeViaApi -ResourceBase $RelationshipTypesBase -Name 'Exclusive' -Slug $Slug -Description 'Primary studio contract (seed)'
}

function Find-PlatformBySlug {
    param([array]$Catalog, [string]$Slug)
    $candidates = @($Slug)
    if ($Slug -eq 'twitter') { $candidates = @('x', 'twitter') }
    $last = $null
    foreach ($c in $candidates) {
        $p = Find-BySlug -Items $Catalog -Slug $c -Label 'Social platform' -Quiet
        if ($null -ne $p) { return $p }
        $last = $c
    }
    Write-Warning "Social platform slug '$Slug' not found (tried: $($candidates -join ', '))."
    return $null
}

function Resolve-TaxonomyIdsWithFallback {
    param([string[]]$Slugs, [string[]]$Types, [array]$Catalog, [string]$PersonName)
    $ids = @()
    if ($Slugs -and $Slugs.Count -gt 0) {
        $ids = Resolve-TaxonomyIds -Slugs $Slugs -Catalog $Catalog
    }
    if ($ids.Count -eq 0 -and $Types -and $Types.Count -gt 0) {
        foreach ($t in $Types) {
            $hit = $Catalog | Where-Object { $_.type -eq $t } | Select-Object -First 1
            if ($null -ne $hit) { $ids += $hit.id }
        }
    }
    if ($ids.Count -gt 0 -or $Catalog.Count -eq 0) { return $ids }
    Write-Warning "No taxonomies matched for $PersonName. Using up to 3 from catalog. Available slugs: $(($Catalog | ForEach-Object { $_.slug }) -join ', ')"
    return @($Catalog | Select-Object -First 3 | ForEach-Object { $_.id })
}

function Resolve-StudioIdsWithFallback {
    param([string[]]$Slugs, [array]$Catalog, [string]$PersonName)
    $ids = Resolve-StudioIds -Slugs $Slugs -Catalog $Catalog
    if ($ids.Count -gt 0 -or $Catalog.Count -eq 0) { return $ids }
    Write-Warning "No studio slugs matched for $PersonName. Using up to 2 active studios from catalog."
    return @($Catalog | Select-Object -First 2 | ForEach-Object { $_.id })
}

function Get-ExistingPersonIdBySlug {
    param([string]$Slug)
    $list = (Invoke-SbpJson -Method GET -Uri $PersonsBase).data
    $hit = $list | Where-Object { $_.slug -eq $Slug } | Select-Object -First 1
    if ($null -eq $hit) { return $null }
    return $hit.id
}

function New-PersonRecord {
    param([hashtable]$Def)
    $slug = $Def.slug
    if ($SkipIfExists) {
        $existingId = Get-ExistingPersonIdBySlug -Slug $slug
        if ($null -ne $existingId) {
            Write-Host "Person '$slug' already exists ($existingId) - linking only." -ForegroundColor Yellow
            return $existingId
        }
    }

    $createBody = @{
        name        = $Def.name
        slug        = $Def.slug
        stageName   = $Def.stageName
        legalName   = $Def.legalName
        bio         = $Def.bio
        birthDate   = $Def.birthDate
        birthPlace  = $Def.birthPlace
        nationality = $Def.nationality
        gender      = $Def.gender
        heightCm    = $Def.heightCm
        weightKg    = $Def.weightKg
        verified    = $Def.verified
        debutYear   = $Def.debutYear
        careerStatus = $Def.careerStatus
        career      = $Def.career
        actor       = $Actor
    }
    $resp = Invoke-SbpJson -Method POST -Uri $PersonsBase -Body $createBody
    if ($resp.code -ne 'PERSON_CREATED') {
        throw "Unexpected create response: $($resp.code)"
    }
    Write-Host "Created person $($Def.name) id=$($resp.data.id)" -ForegroundColor Green
    return $resp.data.id
}

function Add-PersonLinks {
    param(
        [guid]$PersonId,
        [hashtable]$Def,
        [hashtable]$Catalog,
        [guid]$RelationshipTypeId
    )
    foreach ($taxonomyId in $Def.taxonomyIds) {
        try {
            Invoke-SbpJson -Method POST -Uri "$PersonsBase/$PersonId/taxonomies" -Body @{ taxonomyId = $taxonomyId } | Out-Null
            Write-Host "  + taxonomy $taxonomyId"
        }
        catch {
            Write-Warning "  taxonomy assign failed ($taxonomyId): $($_.Exception.Message)"
        }
    }

    foreach ($studioId in $Def.studioIds) {
        try {
            Invoke-SbpJson -Method POST -Uri "$PersonsBase/$PersonId/studios" -Body @{
                studioId           = $studioId
                relationshipTypeId = $RelationshipTypeId
            } | Out-Null
            Write-Host "  + studio $studioId"
        }
        catch {
            Write-Warning "  studio link failed ($studioId): $($_.Exception.Message)"
        }
    }

    foreach ($acct in $Def.socialAccounts) {
        if ($null -eq $acct.platformId) { continue }
        try {
            Invoke-SbpJson -Method POST -Uri "$PersonsBase/$PersonId/social-accounts" -Body @{
                socialPlatformId = $acct.platformId
                username         = $acct.username
                verified         = $acct.verified
                available        = $true
                followersCount   = $acct.followersCount
                actor            = $Actor
            } | Out-Null
            Write-Host "  + social $($acct.username) on $($acct.platformSlug)"
        }
        catch {
            Write-Warning "  social account failed ($($acct.username)): $($_.Exception.Message)"
        }
    }

    if ($null -ne $Def.physical) {
        try {
            Invoke-SbpJson -Method PUT -Uri "$PersonsBase/$PersonId/physical-attributes" -Body $Def.physical | Out-Null
            Write-Host "  + physical attributes"
        }
        catch {
            Write-Warning "  physical attributes failed: $($_.Exception.Message)"
        }
    }

    foreach ($alias in $Def.aliases) {
        try {
            Invoke-SbpJson -Method POST -Uri "$PersonsBase/$PersonId/aliases" -Body @{ alias = $alias } | Out-Null
            Write-Host "  + alias $alias"
        }
        catch {
            Write-Warning "  alias failed ($alias): $($_.Exception.Message)"
        }
    }
}

# --- Person definitions (edit taxonomy/studio/platform slugs to match your DB) ---
$personDefs = @(
    @{
        name          = 'Natasha Nice'
        slug          = 'natasha-nice'
        stageName     = 'Natasha Nice'
        legalName     = 'Natasha Vorne'
        bio           = 'American performer and director; AVN and XBIZ award nominee.'
        birthDate     = '1988-07-28'
        birthPlace    = 'Pasadena, California, USA'
        nationality   = 'American'
        gender        = 'FEMALE'
        heightCm      = 155
        weightKg      = 54
        verified      = $true
        debutYear     = 2008
        careerStatus  = 'ACTIVE'
        roleSlugs     = @('performer', 'director')
        career        = @{
            details    = 'Long-form features and independent production.'
            highlights = @('AVN nominee', 'XBIZ nominee')
        }
        aliases       = @('Natasha Vorne')
        taxonomySlugs = @()
        taxonomyTypes = @('CATEGORY', 'TAG', 'SPECIALTIES')
        studioSlugs   = @('brazzers', 'reality-kings', 'digital-playground')
        social        = @(
            @{ platformSlug = 'x'; username = 'natasha_nice_seed'; verified = $false; followersCount = 500000 }
            @{ platformSlug = 'instagram'; username = 'natasha_nice_ig_seed'; verified = $true; followersCount = 800000 }
        )
        bodyTypeSlug  = 'curvy'
        physical      = @{ bust = 36; waist = 27; hips = 38; unit = 'inches'; eyeColor = 'Brown'; hairColor = 'Brown' }
    }
    @{
        name          = 'Kendra Lust'
        slug          = 'kendra-lust'
        stageName     = 'Kendra Lust'
        legalName     = 'Michele Mason'
        bio           = 'American performer and director; founder of Lust Cinema.'
        birthDate     = '1978-09-18'
        birthPlace    = 'Madison Heights, Michigan, USA'
        nationality   = 'American'
        gender        = 'FEMALE'
        heightCm      = 160
        weightKg      = 56
        verified      = $true
        debutYear     = 2012
        careerStatus  = 'ACTIVE'
        roleSlugs     = @('performer', 'director', 'producer')
        career        = @{
            details    = 'Feature films and director-owned label.'
            highlights = @('Founded Lust Cinema')
        }
        aliases       = @('Michele Mason')
        taxonomySlugs = @()
        taxonomyTypes = @('CATEGORY', 'TAG', 'MOOD_TAG')
        studioSlugs   = @('brazzers', 'vixen-media-group', 'wicked-pictures')
        social        = @(
            @{ platformSlug = 'x'; username = 'kendra_lust_seed'; verified = $true; followersCount = 900000 }
            @{ platformSlug = 'instagram'; username = 'kendra_lust_ig_seed'; verified = $true; followersCount = 1200000 }
        )
        bodyTypeSlug  = 'curvy'
        physical      = @{ bust = 34; waist = 28; hips = 36; unit = 'inches'; eyeColor = 'Brown'; hairColor = 'Brown' }
    }
)

if ($ResetPersonDb) {
    Reset-PersonDatabase
}
Seed-PersonLookupTables

$catalog = Get-ApiCatalog
Write-Host "Catalog: $($catalog.Taxonomies.Count) taxonomies, $($catalog.Studios.Count) studios, $($catalog.Platforms.Count) platforms."
if ($catalog.Taxonomies.Count -eq 0 -or $catalog.Studios.Count -eq 0) {
    Write-Warning "Taxonomies or studios list is empty. Seed reference/studio data first, then re-run."
}

$relationshipTypeId = [guid](Get-RelationshipTypeId -Slug $RelationshipTypeSlug)

foreach ($def in $personDefs) {
    Write-Host "`n=== $($def.name) ===" -ForegroundColor Cyan
    $taxonomyIds = @(Resolve-TaxonomyIdsWithFallback -Slugs $def.taxonomySlugs -Types $def.taxonomyTypes -Catalog $catalog.Taxonomies -PersonName $def.name)
    $roleTaxonomyIds = Resolve-RoleTaxonomyIds -Slugs $def.roleSlugs -Catalog $catalog.Taxonomies
    foreach ($rid in $roleTaxonomyIds) {
        if ($taxonomyIds -notcontains $rid) { $taxonomyIds += $rid }
    }
    $studioIds = Resolve-StudioIdsWithFallback -Slugs $def.studioSlugs -Catalog $catalog.Studios -PersonName $def.name

    $physical = $null
    if ($null -ne $def.physical) {
        $physical = @{}
        foreach ($k in $def.physical.Keys) { $physical[$k] = $def.physical[$k] }
        if ($def.bodyTypeSlug) {
            $btId = Get-BodyTypeIdBySlug -Slug $def.bodyTypeSlug
            if ([string]::IsNullOrWhiteSpace($btId)) {
                Write-Warning "body_type slug '$($def.bodyTypeSlug)' not found for $($def.name)."
            }
            else {
                $physical['bodyTypeId'] = $btId
            }
        }
    }

    $socialAccounts = @()
    foreach ($s in $def.social) {
        $plat = Find-PlatformBySlug -Catalog $catalog.Platforms -Slug $s.platformSlug
        if ($null -eq $plat) { continue }
        $socialAccounts += @{
            platformId     = $plat.id
            platformSlug   = $s.platformSlug
            username       = $s.username
            verified       = $s.verified
            followersCount = $s.followersCount
        }
    }

    $prepared = @{
        name          = $def.name
        slug          = $def.slug
        stageName     = $def.stageName
        legalName     = $def.legalName
        bio           = $def.bio
        birthDate     = $def.birthDate
        birthPlace    = $def.birthPlace
        nationality   = $def.nationality
        gender        = $def.gender
        heightCm      = $def.heightCm
        weightKg      = $def.weightKg
        verified      = $def.verified
        debutYear     = $def.debutYear
        careerStatus  = $def.careerStatus
        career        = $def.career
        taxonomyIds   = $taxonomyIds
        studioIds     = $studioIds
        socialAccounts = $socialAccounts
        physical      = $physical
        aliases       = $def.aliases
    }

    $personId = New-PersonRecord -Def $prepared
    Add-PersonLinks -PersonId $personId -Def $prepared -Catalog $catalog -RelationshipTypeId $relationshipTypeId
}

Write-Host "`nDone. Verify:" -ForegroundColor Green
Write-Host "  .\scripts\run.ps1 start person   # if not running"
Write-Host "  curl.exe -s `"$PersonsBase`" | ConvertFrom-Json | Select-Object -ExpandProperty data"
