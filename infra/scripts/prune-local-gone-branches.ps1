# prune-local-gone-branches.ps1
# Usage: .\prune-local-gone-branches.ps1 [-Force] [-PruneUntracked]
#
# Fetches and prunes remote refs, then deletes local branches
# whose upstream is gone (i.e. deleted on remote).
#
# -Force          : Force delete branches even if not fully merged (git branch -D)
# -PruneUntracked : Also list and optionally delete branches with no upstream tracking at all
#                   Requires confirmation unless combined with -Force
 
param(
  [switch]$Force,
  [switch]$PruneUntracked
)
 
# 1) Update remote refs and prune deleted branches from origin/*
git fetch origin --prune | Out-Host
 
$current = git branch --show-current
 
# 2) Parse all local branches with their upstream tracking info
$allBranches = git branch -vv | ForEach-Object {
  $line = $_ -replace '^\*\s+', '' -replace '^\s+', ''
  $name = ($line -split '\s+')[0]
  $upstream = if ($line -match '\[([^\]]+)\]') { $matches[1] } else { $null }
  [PSCustomObject]@{ Name = $name; Upstream = $upstream }
} | Where-Object { $_.Name -and $_.Name -ne $current }
 
# 3) Find gone branches (upstream explicitly marked as gone)
$goneBranches = $allBranches | Where-Object { $_.Upstream -match 'gone' } | Select-Object -ExpandProperty Name
 
# 4) Find untracked branches (no upstream at all)
$untrackedBranches = $allBranches | Where-Object { -not $_.Upstream } | Select-Object -ExpandProperty Name
 
# -- Handle gone branches ------------------------------------------------------
 
if (-not $goneBranches -or $goneBranches.Count -eq 0) {
  Write-Host "No local branches with upstream=[gone] found." -ForegroundColor Green
} else {
  Write-Host ""
  Write-Host "The following local branches have upstream=[gone] and will be deleted:" -ForegroundColor Yellow
  $goneBranches | ForEach-Object { Write-Host "  - $_" }
  Write-Host ""
 
  foreach ($b in $goneBranches) {
    if ($Force) {
      Write-Host "Force deleting: $b" -ForegroundColor Red
      git branch -D $b | Out-Host
    } else {
      Write-Host "Safe deleting: $b" -ForegroundColor Green
      git branch -d $b | Out-Host
    }
  }
}
 
# -- Handle untracked branches -------------------------------------------------
 
if ($PruneUntracked) {
  if (-not $untrackedBranches -or $untrackedBranches.Count -eq 0) {
    Write-Host "No local branches without upstream tracking found." -ForegroundColor Green
  } else {
    Write-Host ""
    Write-Host "The following local branches have NO upstream tracking:" -ForegroundColor Yellow
    $untrackedBranches | ForEach-Object { Write-Host "  - $_" }
    Write-Host ""
 
    if ($Force) {
      foreach ($b in $untrackedBranches) {
        Write-Host "Force deleting untracked: $b" -ForegroundColor Red
        git branch -D $b | Out-Host
      }
    } else {
      $confirm = Read-Host "Are you sure you want to delete all untracked branches? (y/N)"
      if ($confirm -eq 'y') {
        foreach ($b in $untrackedBranches) {
          Write-Host "Deleting untracked: $b" -ForegroundColor Red
          git branch -D $b | Out-Host
        }
      } else {
        Write-Host "Skipping untracked branches." -ForegroundColor Cyan
      }
    }
  }
}
 
Write-Host ""
Write-Host "Done." -ForegroundColor Cyan