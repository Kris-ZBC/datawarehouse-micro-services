#!/usr/bin/env bash
set -euo pipefail

# fix-package-naming.sh
#
# Scans all Java source files and fixes package path violations:
#   - Uppercase letters        → lowercase
#   - Hyphens (-)              → removed
#   - Underscores (_)          → removed
#   - Segments starting with a digit → prefixed with 'p'
#
# Usage:
#   ./fix-package-naming.sh [--dry-run]
#
# Must be run from the repo root.

DRY_RUN=false
for arg in "$@"; do
    [[ "$arg" == "--dry-run" ]] && DRY_RUN=true
done

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"

TOTAL_FILES=$(git ls-files '*.java' | wc -l | tr -d ' ')
echo "======================================================"
echo " fix-package-naming.sh"
echo " Repo root : $REPO_ROOT"
echo " Java files: $TOTAL_FILES"
echo " Mode      : $( $DRY_RUN && echo 'DRY RUN' || echo 'LIVE' )"
echo "======================================================"
echo ""

MOVED=0
FAILED=0

# ── helpers ────────────────────────────────────────────────────────────────────

normalize_segment() {
    local seg="$1"
    seg="${seg,,}"
    seg="${seg//-/}"
    seg="${seg//_/}"
    if [[ "$seg" =~ ^[0-9] ]]; then seg="p${seg}"; fi
    echo "$seg"
}

normalize_pkg_path() {
    local path="$1" result="" IFS='/'
    read -ra segments <<< "$path"
    for seg in "${segments[@]}"; do
        result="${result:+$result/}$(normalize_segment "$seg")"
    done
    echo "$result"
}

move_dir() {
    local old_dir="$1" new_dir="$2"
    local lower_old lower_new
    lower_old=$(echo "$old_dir" | tr '[:upper:]' '[:lower:]')
    lower_new=$(echo "$new_dir" | tr '[:upper:]' '[:lower:]')

    mkdir -p "$(dirname "$new_dir")"

    if [[ -d "$new_dir" ]] || git ls-files "$new_dir" | grep -q .; then
        echo "        [destination exists — merging files individually]"
        local file_count=0
        git ls-files "$old_dir" | while read -r f; do
            rel="${f#$old_dir/}"
            local target_dir="$new_dir/$(dirname "$rel")"
            mkdir -p "$target_dir"
            git mv "$f" "$new_dir/$rel"
            file_count=$((file_count + 1))
        done
        find "$old_dir" -depth -type d 2>/dev/null | while read -r d; do
            rmdir "$d" 2>/dev/null || true
        done
        return 0
    fi

    if [[ "$lower_old" == "$lower_new" ]]; then
        echo "        [case-only rename — using /tmp intermediary]"
        local tmp_dir="/tmp/git_pkg_fix_$$_$(basename "$old_dir")"
        cp -r "$old_dir" "$tmp_dir"
        git rm -r --cached "$old_dir" > /dev/null
        find "$old_dir" -type f | while read -r f; do rm -f "$f"; done
        find "$old_dir" -depth -type d | while read -r d; do rmdir "$d" 2>/dev/null || true; done
        mkdir -p "$new_dir"
        cp -r "$tmp_dir/." "$new_dir/"
        rm -rf "$tmp_dir"
        git add "$new_dir"
    else
        git mv "$old_dir" "$new_dir"
    fi
}

# ── phase 1: collect violations ───────────────────────────────────────────────

echo "[Phase 1/3] Scanning $TOTAL_FILES Java files for violations..."

declare -A SEEN_DIRS
declare -A PKG_RENAMES

git ls-files '*.java' > /tmp/java_files.txt

SCAN_COUNT=0
while IFS= read -r file; do
    SCAN_COUNT=$((SCAN_COUNT + 1))
    if (( SCAN_COUNT % 100 == 0 )); then
        echo "  ... scanned $SCAN_COUNT / $TOTAL_FILES files"
    fi

    dir=$(dirname "$file")
    if ! echo "$dir" | grep -qE 'src/(main|test)/java'; then continue; fi

    java_root=$(echo "$dir" | grep -oE '^.*/src/(main|test)/java')
    pkg_path=$(echo "$dir" | sed "s|${java_root}/||")
    normalized=$(normalize_pkg_path "$pkg_path")
    if [[ "$pkg_path" == "$normalized" ]]; then continue; fi

    current_old="$java_root"
    current_new="$java_root"
    IFS='/' read -ra segs     <<< "$pkg_path"
    IFS='/' read -ra norm_segs <<< "$normalized"

    for i in "${!segs[@]}"; do
        seg="${segs[$i]}"
        norm_seg="${norm_segs[$i]}"
        old_seg_dir="${current_old}/${seg}"
        new_seg_dir="${current_new}/${norm_seg}"
        if [[ "$seg" != "$norm_seg" ]] && [[ -z "${SEEN_DIRS[$old_seg_dir]+x}" ]]; then
            SEEN_DIRS[$old_seg_dir]="$new_seg_dir"
        fi
        current_old="${current_old}/${seg}"
        current_new="${current_new}/${norm_seg}"
    done

    old_pkg=$(echo "$pkg_path" | tr '/' '.')
    new_pkg=$(echo "$normalized" | tr '/' '.')
    if [[ "$old_pkg" != "$new_pkg" ]]; then
        PKG_RENAMES[$old_pkg]="$new_pkg"
    fi

done < /tmp/java_files.txt

DIR_COUNT=${#SEEN_DIRS[@]}
PKG_COUNT=${#PKG_RENAMES[@]}
echo ""
echo "  Found $DIR_COUNT director(ies) to move"
echo "  Found $PKG_COUNT unique package rename(s)"
echo ""

if [[ $DIR_COUNT -eq 0 ]]; then
    echo "No violations found. Exiting."
    exit 0
fi

# ── phase 2: move directories ─────────────────────────────────────────────────

echo "[Phase 2/3] Moving $DIR_COUNT director(ies)..."
echo ""

mapfile -t sorted_keys < <(
    for key in "${!SEEN_DIRS[@]}"; do
        depth=$(echo "$key" | tr -cd '/' | wc -c)
        echo "${depth} ${key}"
    done | sort -n | awk '{print $2}'
)

MOVE_IDX=0
for old_dir in "${sorted_keys[@]}"; do
    new_dir="${SEEN_DIRS[$old_dir]}"
    MOVE_IDX=$((MOVE_IDX + 1))

    old_pkg=$(echo "$old_dir" | grep -oE 'java/.*' | cut -c6- | tr '/' '.')
    new_pkg=$(echo "$new_dir" | grep -oE 'java/.*' | cut -c6- | tr '/' '.')

    echo "  [$MOVE_IDX/$DIR_COUNT] $old_pkg"
    echo "         →  $new_pkg"

    if $DRY_RUN; then
        echo "        [dry-run: skipping]"
        echo ""
        continue
    fi

    if ! git ls-files "$old_dir" | grep -q .; then
        echo "        [skipped: already moved with parent]"
        echo ""
        continue
    fi

    if move_dir "$old_dir" "$new_dir"; then
        MOVED=$((MOVED + 1))
        file_count=$(git ls-files "$new_dir" | wc -l | tr -d ' ')
        echo "        [OK — $file_count file(s) moved]"
    else
        echo "        [ERROR: move failed]"
        FAILED=$((FAILED + 1))
    fi
    echo ""
done

# ── phase 3: rewrite declarations and imports ─────────────────────────────────

if ! $DRY_RUN && [[ $PKG_COUNT -gt 0 ]]; then
    echo "[Phase 3/3] Rewriting package declarations and imports..."
    echo "  Building sed script from $PKG_COUNT rename(s)..."

    # Build a single sed script with all substitutions — one pass per file
    SED_SCRIPT="/tmp/pkg_renames_$$.sed"
    > "$SED_SCRIPT"
    for old_pkg in "${!PKG_RENAMES[@]}"; do
        new_pkg="${PKG_RENAMES[$old_pkg]}"
        # Escape dots for sed
        escaped_old="${old_pkg//./\\.}"
        echo "s|${escaped_old}|${new_pkg}|g" >> "$SED_SCRIPT"
    done

    echo "  Scanning files..."
    REWRITE_COUNT=0
    FILE_IDX=0
    git ls-files '*.java' | while read -r src; do
        FILE_IDX=$((FILE_IDX + 1))
        if (( FILE_IDX % 100 == 0 )); then
            echo "  ... processed $FILE_IDX / $TOTAL_FILES files ($REWRITE_COUNT updated so far)"
        fi
        if sed -n -f "$SED_SCRIPT" "$src" | grep -q .; then
            sed -i -f "$SED_SCRIPT" "$src"
            REWRITE_COUNT=$((REWRITE_COUNT + 1))
            echo "  updated: $src"
        fi
    done

    rm -f "$SED_SCRIPT"
    echo ""
fi

# ── summary ───────────────────────────────────────────────────────────────────

echo "======================================================"
echo " Done."
if $DRY_RUN; then
    echo " Dry run — no files were changed."
else
    echo " Moved : $MOVED director(ies)"
    echo " Failed: $FAILED director(ies)"
    if [[ $MOVED -gt 0 ]]; then
        echo ""
        echo " Next steps:"
        echo "   git diff --staged"
        echo "   git commit -m \"fix: normalize Java package naming conventions\""
    fi
fi
echo "======================================================"