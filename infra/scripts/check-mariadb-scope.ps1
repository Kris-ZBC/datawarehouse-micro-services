# check-mariadb-scope.ps1
# Scans all bc-*/*-impl/pom.xml files and reports if mariadb-java-client
# is missing runtime scope — only for BCs that contain @Entity annotations.
# Sagas and handlers without @Entity are skipped.
#
# Usage:
#   .\infra\scripts\check-mariadb-scope.ps1

$repoRoot = (git rev-parse --show-toplevel).Trim() -replace '/', '\'
Set-Location $repoRoot

$found  = 0
$issues = 0

Get-ChildItem -Path "bc-*" -Directory | ForEach-Object {
    $bcDir  = $_.FullName
    $bcName = $_.Name

    # Check if BC contains any @Entity annotations — excluding target folders
    # Use -eq $true to avoid null being treated as truthy
    $entityFiles = Get-ChildItem -Path $bcDir -Recurse -Filter "*.java" -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -notmatch "\\target\\" } |
        Select-String -Pattern "@Entity" -Quiet

    $hasEntities = ($entityFiles -eq $true)

    if (-not $hasEntities) {
        Write-Host "SKIPPING - no @Entity found: $bcName" -ForegroundColor DarkGray
        return
    }

    # Find the *-impl directory
    $implDir = Get-ChildItem -Path $bcDir -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match "-impl" } |
        Select-Object -First 1

    if (-not $implDir) {
        Write-Host "SKIPPING - no *-impl directory found: $bcName" -ForegroundColor DarkGray
        return
    }

    $implPom = Get-ChildItem -Path $implDir.FullName -Filter "pom.xml" -ErrorAction SilentlyContinue |
        Select-Object -First 1

    if (-not $implPom) {
        Write-Host "SKIPPING - no pom.xml in $($implDir.Name): $bcName" -ForegroundColor DarkGray
        return
    }

    $rel = $implPom.FullName.Replace($repoRoot + "\", "")
    $found++

    [xml]$xml = Get-Content $implPom.FullName -Encoding UTF8
    $dependencies = $xml.project.dependencies.dependency

    $mariadb = $dependencies | Where-Object {
        $_.artifactId -eq "mariadb-java-client"
    }

    if (-not $mariadb) {
        Write-Host "MISSING  - mariadb-java-client not declared: $rel" -ForegroundColor Red
        $issues++
        return
    }

    $scope = $mariadb.scope

    if ($scope -eq "runtime") {
        Write-Host "OK       - $rel" -ForegroundColor Green
    } else {
        $current = if ($scope) { $scope } else { "compile (default)" }
        Write-Host "FAIL     - missing runtime scope (currently: $current): $rel" -ForegroundColor Red
        $issues++
    }
}

Write-Host ""
Write-Host "======================================================"
Write-Host " Scanned : $found pom.xml file(s) (with @Entity only)"
Write-Host " Issues  : $issues"
Write-Host "======================================================"

if ($issues -gt 0) {
    exit 1
}