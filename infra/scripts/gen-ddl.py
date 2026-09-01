#!/usr/bin/env python3
"""
gen-ddl.py
==========
Scans bc-* directories for JPA @Entity classes and generates
Flyway-compatible DDL migration files (V1__init.sql) for each microservice.

Usage:
    python3 infra/scripts/gen-ddl.py [bc-name] [--dry-run] [--force]

Arguments:
    bc-name     Optional. Target a specific BC (e.g. bc-consent).
                If omitted, all bc-* directories are processed.

Options:
    --dry-run   Print generated SQL to stdout instead of writing files
    --force     Overwrite existing migration files without prompting

Must be run from $PROJECT_ROOT (repo root).
"""

import os, re, sys
from datetime import datetime, timezone
from pathlib import Path

REPO_ROOT = Path(os.getcwd())
DRY_RUN   = "--dry-run" in sys.argv
FORCE     = "--force"   in sys.argv
TARGET_BC = next((a for a in sys.argv[1:] if not a.startswith("--")), None)

# ---------------------------------------------------------------------------
# Findings collector
# ---------------------------------------------------------------------------

findings = []  # list of (bc_name, entity_file, finding_type, detail)

def add_finding(bc_name, entity_file, finding_type, detail):
    findings.append((bc_name, entity_file, finding_type, detail))

# ---------------------------------------------------------------------------
# Java type -> SQL type mapping
# ---------------------------------------------------------------------------
JAVA_TO_SQL = {
    "UUID":           "BINARY(16)",
    "String":         "VARCHAR(255)",
    "Long":           "BIGINT",
    "long":           "BIGINT",
    "Integer":        "INT",
    "int":            "INT",
    "Boolean":        "BOOLEAN",
    "boolean":        "BOOLEAN",
    "Instant":        "TIMESTAMP",
    "OffsetDateTime": "DATETIME(6)",
    "LocalDate":      "DATE",
    "LocalDateTime":  "DATETIME",
    "Double":         "DOUBLE",
    "double":         "DOUBLE",
    "Float":          "FLOAT",
    "float":          "FLOAT",
    "BigDecimal":     "DECIMAL(19,4)",
}

def camel_to_snake(name):
    s = re.sub(r'([A-Z]+)([A-Z][a-z])', r'\1_\2', name)
    s = re.sub(r'([a-z\d])([A-Z])', r'\1_\2', s)
    return s.lower()

def sql_type_for(java_type, col_def, length, is_enum, is_uuid_convert):
    if col_def:
        return col_def
    if is_uuid_convert or java_type == "UUID":
        return "BINARY(16)"
    if is_enum:
        return "VARCHAR(255)"
    sql = JAVA_TO_SQL.get(java_type, "VARCHAR(255)")
    if sql == "VARCHAR(255)" and length:
        sql = f"VARCHAR({length})"
    return sql

def extract_annotation_body(text, annotation):
    pat = re.compile(annotation + r'\s*\(')
    m = pat.search(text)
    if not m:
        return None
    start = m.end()
    depth = 1
    i = start
    in_quote = False
    while i < len(text) and depth > 0:
        c = text[i]
        if c == '"' and (i == 0 or text[i-1] != '\\'):
            in_quote = not in_quote
        elif not in_quote:
            if c == '(':
                depth += 1
            elif c == ')':
                depth -= 1
        i += 1
    return text[start:i-1]

def extract_all_annotation_bodies(text, annotation):
    results = []
    search_from = 0
    pat = re.compile(annotation + r'\s*\(')
    while True:
        m = pat.search(text, search_from)
        if not m:
            break
        start = m.end()
        depth = 1
        i = start
        in_quote = False
        while i < len(text) and depth > 0:
            c = text[i]
            if c == '"' and (i == 0 or text[i-1] != '\\'):
                in_quote = not in_quote
            elif not in_quote:
                if c == '(':
                    depth += 1
                elif c == ')':
                    depth -= 1
            i += 1
        results.append(text[start:i-1])
        search_from = i
    return results

# ---------------------------------------------------------------------------
# Parse @Embeddable class and return its columns
# ---------------------------------------------------------------------------

def parse_embeddable(class_name, search_root, bc_name):
    """Find and parse an @Embeddable class, returning its columns."""
    candidates = list(search_root.rglob(f"{class_name}.java"))
    if not candidates:
        # Broaden search to entire repo
        candidates = list(REPO_ROOT.rglob(f"{class_name}.java"))

    if not candidates:
        add_finding(bc_name, class_name, "MISSING_EMBEDDABLE",
            f"Could not find @Embeddable class '{class_name}' — columns not expanded")
        return [{"name": f"-- TODO: expand {class_name}", "type": "",
                 "not_null": True, "pk": True, "unique": False, "default": None}]

    raw = candidates[0].read_text(encoding="utf-8", errors="replace").replace("\r\n", "\n")

    cols = []
    field_re = re.compile(
        r'((?:[ \t]*@[^\n]+\n)+)'
        r'[ \t]*(?:private|protected|public)?\s+'
        r'(\w+)(?:<[^>]+>)?\s+(\w+)\s*[=;]',
        re.MULTILINE
    )

    for m in field_re.finditer(raw):
        annotations = m.group(1)
        java_type   = m.group(2)
        field_name  = m.group(3)

        if java_type in ("class", "interface", "enum", "void", "static",
                         "final", "return", "new"):
            continue

        col_bodies = extract_all_annotation_bodies(annotations, r'@Column')
        col_body = col_bodies[-1] if col_bodies else ""

        cn_m = re.search(r'(?<![a-zA-Z])name\s*=\s*"([^"]+)"', col_body)
        cd_m = re.search(r'columnDefinition\s*=\s*"([^"]+)"', col_body)
        cl_m = re.search(r'(?<![a-zA-Z])length\s*=\s*(\d+)', col_body)

        col_name = cn_m.group(1) if cn_m else camel_to_snake(field_name)
        col_def  = cd_m.group(1) if cd_m else None
        length   = cl_m.group(1) if cl_m else None

        is_uuid_convert = java_type == "UUID"
        sql_t = sql_type_for(java_type, col_def, length, False, is_uuid_convert)

        cols.append({
            "name":     col_name,
            "type":     sql_t,
            "not_null": True,
            "pk":       True,
            "unique":   False,
            "default":  None
        })

    return cols

# ---------------------------------------------------------------------------
# Build class -> table map (first pass)
# ---------------------------------------------------------------------------

def build_class_to_table(entity_files):
    """First pass: build a map of Java class name -> SQL table name for all entities."""
    class_to_table = {}
    for path in entity_files:
        raw = path.read_text(encoding="utf-8", errors="replace").replace("\r\n", "\n")
        if not re.search(r'@Entity\b', raw):
            continue
        cm = re.search(r'public\s+(?:class|interface)\s+(\w+)', raw)
        if not cm:
            continue
        class_name = cm.group(1)
        table_body = extract_annotation_body(raw, r'@Table')
        if not table_body:
            continue
        nm = re.search(r'(?<![a-zA-Z])name\s*=\s*"([^"]+)"', table_body)
        if nm:
            class_to_table[class_name] = nm.group(1)
    return class_to_table

# ---------------------------------------------------------------------------
# Parse a single entity file
# ---------------------------------------------------------------------------

def parse_entity(path, bc_name, class_to_table=None):
    raw = path.read_text(encoding="utf-8", errors="replace").replace("\r\n", "\n")

    if not re.search(r'@Entity\b', raw):
        return None

    table_body = extract_annotation_body(raw, r'@Table')
    if not table_body:
        return None
    nm = re.search(r'(?<![a-zA-Z])name\s*=\s*"([^"]+)"', table_body)
    if not nm:
        return None
    table_name = nm.group(1)

    table_ucs = []
    uc_m = re.search(r'uniqueConstraints\s*=\s*@UniqueConstraint\s*\(\s*columnNames\s*=\s*\{([^}]+)\}', table_body)
    if uc_m:
        cols = [c.strip().strip('"') for c in uc_m.group(1).split(",")]
        table_ucs.append(cols)

    columns        = []
    fk_references  = []
    collection_tables = []
    has_many_to_one   = False
    has_version       = False
    nullable_columns  = []

    field_re = re.compile(
        r'((?:[ \t]*@[^\n]+\n)+)'
        r'[ \t]*(?:private|protected|public)?\s+'
        r'(\w+)(?:<[^>]+>)?\s+(\w+)\s*[=;]',
        re.MULTILINE
    )

    for m in field_re.finditer(raw):
        annotations = m.group(1)
        java_type   = m.group(2)
        field_name  = m.group(3)

        if java_type in ("class", "interface", "enum", "void", "static",
                         "final", "return", "new", "Builder", "Set", "List",
                         "Map", "Collection", "Override"):
            continue

        is_id          = bool(re.search(r'@Id\b', annotations))
        is_version     = bool(re.search(r'@Version\b', annotations))
        is_many_one    = bool(re.search(r'@ManyToOne\b', annotations))
        is_one_many    = bool(re.search(r'@OneToMany\b', annotations))
        is_elem        = bool(re.search(r'@ElementCollection\b', annotations))
        is_enum        = bool(re.search(r'@Enumerated\s*\(\s*EnumType\.STRING\s*\)', annotations))
        is_convert     = bool(re.search(r'@Convert\b', annotations))
        is_embedded_id = bool(re.search(r'@EmbeddedId\b', annotations))

        if is_version:
            has_version = True

        if is_one_many:
            continue

        # ── @EmbeddedId — expand the @Embeddable class inline ─────────────────
        if is_embedded_id:
            embeddable_cols = parse_embeddable(java_type, path.parent, bc_name)
            for col in embeddable_cols:
                col["pk"]       = True
                col["not_null"] = True
                columns.append(col)
            continue  # skip normal column processing for this field

        if is_elem:
            ct_body = extract_annotation_body(annotations, r'@CollectionTable')
            if ct_body:
                tn_m = re.search(r'(?<![a-zA-Z])name\s*=\s*"([^"]+)"', ct_body)
                jn_m = re.search(r'joinColumns\s*=\s*@JoinColumn\s*\(\s*name\s*=\s*"([^"]+)"', ct_body)
                if tn_m:
                    child = tn_m.group(1)
                    jcol  = jn_m.group(1) if jn_m else f"{table_name}_id"
                    collection_tables.append((child, jcol))
            continue

        if is_many_one:
            has_many_to_one = True
            jc_body  = extract_annotation_body(annotations, r'@JoinColumn')
            col_body = jc_body or ""
            jn_m     = re.search(r'(?<![a-zA-Z])name\s*=\s*"([^"]+)"', col_body)
            col_name = jn_m.group(1) if jn_m else f"{field_name}_id"
            null_m   = re.search(r'nullable\s*=\s*(true|false)', col_body)
            cd_m     = re.search(r'columnDefinition\s*=\s*"([^"]+)"', col_body)
            not_null = (null_m.group(1) == "false") if null_m else True
            sql_t    = cd_m.group(1) if cd_m else "BINARY(16)"
            ref_table = (class_to_table or {}).get(java_type, camel_to_snake(java_type.replace("Entity", "")))
            columns.append({
                "name": col_name, "type": sql_t, "not_null": not_null,
                "pk": False, "unique": False, "default": None
            })
            fk_references.append((col_name, ref_table))
            continue

        if is_version:
            col_body = extract_annotation_body(annotations, r'@Column')
            col_name = "version"
            if col_body:
                nm2 = re.search(r'(?<![a-zA-Z])name\s*=\s*"([^"]+)"', col_body)
                if nm2:
                    col_name = nm2.group(1)
            columns.append({
                "name": col_name, "type": "BIGINT", "not_null": True,
                "pk": False, "unique": False, "default": "0"
            })
            continue

        col_bodies = extract_all_annotation_bodies(annotations, r'@Column')
        col_body = col_bodies[-1] if col_bodies else ""

        cn_m = re.search(r'(?<![a-zA-Z])name\s*=\s*"([^"]+)"', col_body)
        cd_m = re.search(r'columnDefinition\s*=\s*"([^"]+)"', col_body)
        cl_m = re.search(r'(?<![a-zA-Z])length\s*=\s*(\d+)', col_body)
        nl_m = re.search(r'nullable\s*=\s*(true|false)', col_body)
        uq_m = re.search(r'(?<![a-zA-Z])unique\s*=\s*(true|false)', col_body)

        col_name = cn_m.group(1) if cn_m else field_name
        col_def  = cd_m.group(1) if cd_m else None
        length   = cl_m.group(1) if cl_m else None
        not_null = (nl_m.group(1) == "false") if nl_m else False
        unique   = (uq_m.group(1) == "true")  if uq_m else False

        if is_id:
            not_null = True

        if not is_id and not not_null and col_body:
            nullable_columns.append(col_name)

        is_uuid_convert = is_convert and java_type == "UUID"
        sql_t = sql_type_for(java_type, col_def, length, is_enum, is_uuid_convert)

        columns.append({
            "name": col_name, "type": sql_t, "not_null": not_null,
            "pk": is_id, "unique": unique, "default": None
        })

    # Auto-inject version if missing
    if not has_version:
        existing_names = {c["name"] for c in columns}
        if "version" not in existing_names:
            id_idx = next((i for i, c in enumerate(columns) if c["pk"]), 0)
            columns.insert(id_idx + 1, {
                "name": "version", "type": "BIGINT", "not_null": True,
                "pk": False, "unique": False, "default": "0",
                "_auto_injected": True
            })
            add_finding(bc_name, path.name, "MISSING_VERSION",
                f"Add @Version field to {path.stem}:\n"
                f"        @Version\n"
                f"        private Long version;")

    for col in nullable_columns:
        add_finding(bc_name, path.name, "NULLABLE_COLUMN",
            f"Column '{col}' in {path.stem} is missing nullable = false.\n"
            f"        Consider: @Column(name = \"{col}\", nullable = false)")

    if len(class_to_table) > 1:
        fk_col_names = {col_name for col_name, _ in fk_references}
        for col in columns:
            name = col["name"]
            if col["type"] == "BINARY(16)" and not col["pk"]:
                if name.endswith("_id") and name not in fk_col_names:
                    add_finding(bc_name, path.name, "POSSIBLE_MISSING_RELATION",
                        f"Column \'{name}\' in {path.stem} looks like an intra-BC FK\n"
                        f"        but has no @ManyToOne backing it.\n"
                        f"        Consider: @ManyToOne @JoinColumn(name = \"{name}\", nullable = false)")

    return {
        "table":       table_name,
        "columns":     columns,
        "table_ucs":   table_ucs,
        "fk_refs":     fk_references,
        "coll_tables": collection_tables,
        "has_m2o":     has_many_to_one,
    }

# ---------------------------------------------------------------------------
# SQL generator
# ---------------------------------------------------------------------------

def generate_sql(entities):
    ts = datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ')
    lines = [
        "-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.",
        f"-- Generated by infra/scripts/gen-ddl.py at {ts}",
        "-- Review carefully before applying to a production database.",
        "",
    ]

    ordered = sorted(entities, key=lambda e: (1 if e["has_m2o"] else 0))

    for e in ordered:
        col_defs    = []
        constraints = []

        # Determine if this table uses a composite PK (@EmbeddedId)
        pk_cols = [col["name"] for col in e["columns"] if col["pk"]]
        is_composite_pk = len(pk_cols) > 1

        for col in e["columns"]:
            parts = [f"    {col['name']}", col["type"]]
            if col["not_null"]:
                parts.append("NOT NULL")
            if col["default"] is not None:
                parts.append(f"DEFAULT {col['default']}")
            # Only add inline PRIMARY KEY for single-column PKs
            if col["pk"] and not is_composite_pk:
                parts.append("PRIMARY KEY")
            if col["unique"] and not col["pk"]:
                parts.append("UNIQUE")
            col_line = " ".join(parts)
            if col.get("_auto_injected"):
                col_line += "  -- auto-injected: add @Version to entity"
            col_defs.append(col_line)

        # Add composite PK as table-level constraint
        if is_composite_pk:
            constraints.insert(0,
                f"    PRIMARY KEY ({', '.join(pk_cols)})"
            )

        for col_name, ref_table in e["fk_refs"]:
            constraints.append(
                f"    CONSTRAINT fk_{e['table']}_{col_name}"
                f" FOREIGN KEY ({col_name}) REFERENCES {ref_table}(id)"
            )

        for uc_cols in e["table_ucs"]:
            uc_name = f"uk_{e['table']}_{'_'.join(uc_cols)}"
            constraints.append(
                f"    CONSTRAINT {uc_name} UNIQUE ({', '.join(uc_cols)})"
            )

        all_defs = col_defs + constraints
        lines.append(f"CREATE TABLE IF NOT EXISTS {e['table']} (")
        formatted = []
        for idx, defn in enumerate(all_defs):
            if idx < len(all_defs) - 1:
                if " --" in defn:
                    pre, comment = defn.split(" --", 1)
                    formatted.append(pre.rstrip() + ", --" + comment)
                else:
                    formatted.append(defn + ",")
            else:
                formatted.append(defn)
        lines.append("\n".join(formatted))
        lines.append(");")
        lines.append("")

        for child_table, join_col in e["coll_tables"]:
            lines.append(f"CREATE TABLE IF NOT EXISTS {child_table} (")
            lines.append(f"    {join_col} BINARY(16) NOT NULL,")
            lines.append(f"    -- TODO: add element columns for {child_table}")
            lines.append(
                f"    CONSTRAINT fk_{child_table}_{join_col}"
                f" FOREIGN KEY ({join_col}) REFERENCES {e['table']}(id)"
            )
            lines.append(");")
            lines.append("")

    return "\n".join(lines)

# ---------------------------------------------------------------------------
# Print findings summary
# ---------------------------------------------------------------------------

def print_findings():
    if not findings:
        print("\n✔  No findings — all entities look good!")
        return

    print("\n" + "=" * 60)
    print("FINDINGS SUMMARY")
    print("=" * 60)

    by_bc = {}
    for bc_name, entity_file, finding_type, detail in findings:
        by_bc.setdefault(bc_name, []).append((entity_file, finding_type, detail))

    for bc_name, items in sorted(by_bc.items()):
        print(f"\n  [{bc_name}]")
        for entity_file, finding_type, detail in items:
            label = {
                "MISSING_VERSION":           "MISSING @Version",
                "NULLABLE_COLUMN":           "NULLABLE column",
                "POSSIBLE_MISSING_RELATION": "POSSIBLE missing @ManyToOne",
                "MISSING_EMBEDDABLE":        "MISSING @Embeddable class",
            }.get(finding_type, finding_type)
            print(f"    [{label}] {entity_file}")
            print(f"        {detail}")

    print("\n" + "=" * 60)
    print(f"Total findings: {len(findings)}")
    print("=" * 60)

# ---------------------------------------------------------------------------
# File writing
# ---------------------------------------------------------------------------

def write_sql(bc_dir, sql):
    impl_dirs = list(bc_dir.glob("*-impl"))
    if not impl_dirs:
        print(f"  [skip] {bc_dir.name} — no *-impl directory found")
        return

    migration_dir = impl_dirs[0] / "src/main/resources/db/migration"
    migration_dir.mkdir(parents=True, exist_ok=True)

    existing = list(set(
    list(migration_dir.glob("V*__*.sql")) + 
    list(migration_dir.glob("V*_*.sql"))
))

    if existing and not FORCE:
        print(f"  [skip] {bc_dir.name} — V1__init.sql already exists.")
        print(f"         For schema changes write a new Vn__ migration manually.")
        return

    if existing and FORCE:
        print(f"  [WARNING] --force specified for {bc_dir.name}.")
        print(f"  [WARNING] Overwriting V1__init.sql will cause a Flyway checksum mismatch.")
        print(f"  [WARNING] You MUST drop the flyway_schema_history table or the entire")
        print(f"  [WARNING] database before redeploying, otherwise the application will")
        print(f"  [WARNING] refuse to start.")
        print(f"  [WARNING] Existing files: {', '.join(f.name for f in existing)}")
        for f in existing:
            f.unlink()
        print(f"  [deleted] existing migration(s)")

    out_path = migration_dir / "V1__init.sql"
    out_path.write_text(sql, encoding="utf-8")
    print(f"  [written] {out_path.relative_to(REPO_ROOT)}")

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def find_entities(bc_dir):
    result = []
    for f in bc_dir.rglob("*Entity.java"):
        parts = f.parts
        if any(p in parts for p in ("test", "target", "factory")):
            continue
        if "Factory" in f.name:
            continue
        result.append(f)
    return sorted(result)

def process_bc(bc_dir):
    entity_files = find_entities(bc_dir)
    if not entity_files:
        return

    class_to_table = build_class_to_table(entity_files)
    entities = [parse_entity(ef, bc_dir.name, class_to_table) for ef in entity_files]
    entities = [e for e in entities if e]
    if not entities:
        return

    sql = generate_sql(entities)

    if DRY_RUN:
        print("=" * 60)
        print(f"-- BC: {bc_dir.name}")
        print("=" * 60)
        print(sql)
        return

    write_sql(bc_dir, sql)

def main():
    print(f"Scanning from: {REPO_ROOT}\n")

    if TARGET_BC:
        bc_dir = REPO_ROOT / TARGET_BC
        if not bc_dir.is_dir():
            print(f"Error: '{TARGET_BC}' not found in {REPO_ROOT}")
            sys.exit(1)
        process_bc(bc_dir)
    else:
        for bc_dir in sorted(REPO_ROOT.glob("bc-*/")):
            process_bc(bc_dir)

    print_findings()

    if not DRY_RUN:
        print("\nDone.")

if __name__ == "__main__":
    main()