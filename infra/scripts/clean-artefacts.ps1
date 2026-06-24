# clean-artifacts.ps1
# Usage: .\infra\scripts\clean-artifacts.ps1
# Run once from $PROJECT_ROOT to remove all generated artifacts:
#   - Dockerfiles from bc-* directories
#   - docker-compose.yml / docker-compose.yaml
#   - docker-infra.yml / docker-infra.yaml
# These are all generated dynamically by the pipeline and should not be committed.

$deleted = 0

# Delete Dockerfiles from all bc-* directories
Get-ChildItem -Directory -Filter "bc-*" | ForEach-Object {
    Get-ChildItem -Path $_.FullName -Recurse -Filter "Dockerfile" | ForEach-Object {
        Remove-Item $_.FullName -Force
        Write-Host "Deleted $($_.FullName)" -ForegroundColor Yellow
        $deleted++
    }
}

# Delete generated compose and infra yml files from root
@("docker-compose.yml", "docker-compose.yaml", "docker-infra.yml", "docker-infra.yaml") | ForEach-Object {
    if (Test-Path $_) {
        Remove-Item $_ -Force
        Write-Host "Deleted $_" -ForegroundColor Yellow
        $deleted++
    }
}

Write-Host ""
Write-Host "Done - $deleted file(s) deleted." -ForegroundColor Cyan