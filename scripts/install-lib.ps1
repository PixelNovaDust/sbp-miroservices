# Installs sbp-lib-common into %USERPROFILE%\.m2 (no global Maven required).
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

$Mvnw = Join-Path $Root "mvnw.cmd"
if (Test-Path $Mvnw) {
	& $Mvnw -f sbp-lib-common/pom.xml clean install -DskipTests
	exit $LASTEXITCODE
}

Write-Host "mvnw.cmd not found; using Docker Maven..."
docker run --rm `
	-v "${Root}:/ws" `
	-v "${env:USERPROFILE}/.m2:/root/.m2" `
	-w /ws maven:3.9-eclipse-temurin-17 `
	mvn -f sbp-lib-common/pom.xml clean install -DskipTests
