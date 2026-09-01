#!/usr/bin/env python3
"""
fix-modifying.py
================
Scans all Spring Data JPA repository interfaces for @Modifying annotations
on UPDATE queries and ensures clearAutomatically = true is set.

Usage:
    python3 infra/scripts/fix-modifying.py [--dry-run]

Options:
    --dry-run   Show what would be changed without writing files

Must be run from $PROJECT_ROOT (repo root).

Rules:
    - Only processes methods that have BOTH @Modifying AND @Query with UPDATE
    - Skips @Modifying already containing clearAutomatically = true
    - Skips DELETE queries (clearAutomatically is less relevant there)
    - Preserves all existing formatting and other annotation attributes
"""

import os, re, sys
from pathlib import Path

REPO_ROOT = Path(os.getcwd())
DRY_RUN   = "--dry-run" in sys.argv

# ---------------------------------------------------------------------------
# Patterns
# ---------------------------------------------------------------------------

# Matches @Modifying with no attributes or existing attributes — but NOT already
# containing clearAutomatically
RE_MODIFYING_BARE    = re.compile(r'@Modifying\s*$', re.MULTILINE)
RE_MODIFYING_PARENS  = re.compile(r'@Modifying\s*\(([^)]*)\)')
RE_QUERY_UPDATE      = re.compile(r'@Query\s*[(\s].*?(?:UPDATE|update)', re.DOTALL)
RE_CLEAR_AUTO        = re.compile(r'clearAutomatically\s*=\s*true')

# ---------------------------------------------------------------------------
# Process a single file
# ---------------------------------------------------------------------------

def process_file(path: Path) -> bool:
    """Returns True if the file was modified."""
    content = path.read_text(encoding="utf-8", errors="replace")
    original = content

    # Normalize line endings for processing
    content = content.replace("\r\n", "\n")

    # Split into method blocks — look for @Modifying ... @Query ... method signature
    # Strategy: find each @Modifying and check if an UPDATE @Query follows within
    # a reasonable window (500 chars), then patch the @Modifying

    result = []
    i = 0
    modified = False

    while i < len(content):
        # Find next @Modifying
        m = re.search(r'@Modifying', content[i:])
        if not m:
            result.append(content[i:])
            break

        start = i + m.start()
        result.append(content[i:start])

        # Extract the full @Modifying annotation
        mod_text_start = start
        after_mod = content[start:]

        # Check if it has parens
        paren_m = re.match(r'@Modifying\s*\(([^)]*)\)', after_mod)
        bare_m  = re.match(r'@Modifying\b(?!\s*\()', after_mod)

        if paren_m:
            mod_annotation = paren_m.group(0)
            mod_body       = paren_m.group(1)
            mod_end        = start + paren_m.end()
        elif bare_m:
            mod_annotation = bare_m.group(0)
            mod_body       = None
            mod_end        = start + bare_m.end()
        else:
            # Shouldn't happen
            result.append(content[start:start+10])
            i = start + 10
            continue

        # Look ahead for @Query with UPDATE within next 600 chars
        lookahead = content[mod_end:mod_end + 600]
        is_update = bool(re.search(r'@Query\b.*?(?:UPDATE|update)', lookahead, re.DOTALL))
        already_has_clear = RE_CLEAR_AUTO.search(mod_annotation)

        if is_update and not already_has_clear:
            # Patch the annotation
            if mod_body is not None:
                # Has parens — add to existing body
                existing = mod_body.strip()
                if existing:
                    new_annotation = f"@Modifying({existing}, clearAutomatically = true)"
                else:
                    new_annotation = "@Modifying(clearAutomatically = true)"
            else:
                # Bare @Modifying — add parens
                new_annotation = "@Modifying(clearAutomatically = true)"

            result.append(new_annotation)
            modified = True
        else:
            # No change needed
            result.append(mod_annotation)

        i = mod_end

    if not modified:
        return False

    new_content = "".join(result)

    # Restore original line endings if file had CRLF
    if "\r\n" in original:
        new_content = new_content.replace("\n", "\r\n")

    if DRY_RUN:
        print(f"  [would patch] {path.relative_to(REPO_ROOT)}")
        # Show diff summary
        orig_lines = original.replace("\r\n", "\n").splitlines()
        new_lines  = new_content.replace("\r\n", "\n").splitlines()
        for j, (ol, nl) in enumerate(zip(orig_lines, new_lines)):
            if ol != nl:
                print(f"    line {j+1}: {ol.strip()}")
                print(f"           → {nl.strip()}")
    else:
        path.write_text(new_content, encoding="utf-8")
        print(f"  [patched] {path.relative_to(REPO_ROOT)}")

    return True

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def find_repositories():
    result = []
    for f in REPO_ROOT.rglob("*SpringDataRepository*.java"):
        parts = f.parts
        if any(p in parts for p in ("target", "test")):
            continue
        result.append(f)
    # Also catch any JpaRepository interfaces not following the naming convention
    for f in REPO_ROOT.rglob("*.java"):
        parts = f.parts
        if any(p in parts for p in ("target", "test")):
            continue
        if f in result:
            continue
        content = f.read_text(encoding="utf-8", errors="replace")
        if "@Modifying" in content and "JpaRepository" in content:
            result.append(f)
    return sorted(set(result))

def main():
    print(f"Scanning from: {REPO_ROOT}")
    print(f"Mode: {'DRY RUN' if DRY_RUN else 'WRITE'}\n")

    files = find_repositories()
    patched = 0
    skipped = 0

    for path in files:
        content = path.read_text(encoding="utf-8", errors="replace")
        if "@Modifying" not in content:
            continue
        if process_file(path):
            patched += 1
        else:
            skipped += 1
            print(f"  [ok]      {path.relative_to(REPO_ROOT)}")

    print(f"\nDone - {patched} file(s) patched, {skipped} already correct.")

if __name__ == "__main__":
    main()