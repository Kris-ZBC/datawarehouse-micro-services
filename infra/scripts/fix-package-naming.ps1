# fix-package-naming.ps1
#
# PowerShell wrapper for fix-package-naming.sh
#
# Usage:
#   .\fix-package-naming.ps1
#   .\fix-package-naming.ps1 -DryRun
#
# Requires Git Bash (Git for Windows) to be installed.

param(
    [switch]$DryRun
)

# ── locate Git Bash ────────────────────────────────────────────────────────────

$gitBashPaths = @(
    "C:\Program Files\Git\bin\bash.exe",
    "C:\Program Files (x86)\Git\bin\bash.exe",
    "$env:LOCALAPPDATA\Programs\Git\bin\bash.exe"
)

$bash = $gitBashPaths | Where-Object { Test-Path $_ } | Select-Object -First 1

if (-not $bash) {
    Write-Error "Git Bash not found. Please install Git for Windows."
    exit 1
}

# ── locate the shell script ────────────────────────────────────────────────────

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$shScript = Join-Path $scriptDir "fix-package-naming.sh"

if (-not (Test-Path $shScript)) {
    Write-Error "fix-package-naming.sh not found next to this script at: $shScript"
    exit 1
}

# Convert Windows path to Unix path for bash (C:\foo\bar → /c/foo/bar)
$shScriptUnix = $shScript -replace '\\', '/'
$driveLetter = $shScriptUnix.Substring(0, 1).ToLower()
$shScriptUnix = '/' + $driveLetter + $shScriptUnix.Substring(2)

# ── run ────────────────────────────────────────────────────────────────────────

$bashArgs = ""
if ($DryRun) {
    $bashArgs = "--dry-run"
    Write-Host "Running in dry-run mode..." -ForegroundColor Yellow
} else {
    Write-Host "Running fix-package-naming.sh..." -ForegroundColor Cyan
}

& $bash -c "bash '$shScriptUnix' $bashArgs"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Script failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}