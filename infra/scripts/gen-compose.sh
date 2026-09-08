#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./infra/scripts/gen-compose.sh > docker-compose.yaml
#
# Must be run from $PROJECT_ROOT (repo root).
#
# Optional env:
#   REGISTRY_IMAGE       (default: local)
#   IMAGE_TAG            (default: dev)
#   CERTS_DIR            (default: /opt/projects/sopinfo/certs)
#   SEARCH_DEPTH         (default: 3)
#   COMPOSE_PROJECT_NAME (optional)

REGISTRY_IMAGE="${REGISTRY_IMAGE:-local}"
IMAGE_TAG="${IMAGE_TAG:-dev}"
SEARCH_DEPTH="${SEARCH_DEPTH:-3}"
CERTS_DIR="${CERTS_DIR:-/opt/projects/sopinfo/certs}"
# Must match gen-db-provision.sh's own default exactly — both scripts
# derive db_name/db_user/db_pass independently (no shared state file),
# so they only agree if every default and every env var name here is
# kept identical between the two.
#
# PROJECT_NAME (not a script-invented name): this is the exact env var
# DWH's shared-lib application-shared.properties reads for
# sop.application.name, which is what db.name's prefix actually
# resolves to at runtime (default: DWH — not sopinfo, that default
# lives in common-data and is a leftover from before DWH forked off
# the SopInfo repo; DWH's own shared-lib already overrides it).
PROJECT_NAME="${PROJECT_NAME:-DWH}"
DB_PASSWORD_SUFFIX="${DB_PASSWORD_SUFFIX:-localdev}"

# ── helpers ────────────────────────────────────────────────────────────────────

service_name_from_bc() {
    basename "$1" | sed 's/^bc-//'
}

find_app_props() {
    local bc_dir="$1"
    find "$bc_dir" -maxdepth 6 -type f -name application.properties 2>/dev/null | sort | head -n 1 || true
}

# Extract the default value from a property like:
#   bc.qualifier=${BC_QUALIFIER:person}  →  person
# Mirrors gen-db-provision.sh's extract_default exactly.
extract_default() {
    local props="$1" key="$2"
    [[ -f "$props" ]] || return 0
    local line
    line="$(grep -E "^[[:space:]]*${key}[[:space:]]*=" "$props" | head -n 1 | tr -d '\r' || true)"
    [[ -n "$line" ]] || return 0
    local val
    val="$(echo "$line" | sed -nE 's/.*\$\{[^:}]+:([^}]+)\}.*/\1/p')"
    if [[ -z "$val" ]]; then
        val="$(echo "$line" | sed -nE 's/^[^=]+=\s*(.*)/\1/p' | tr -d ' ')"
    fi
    echo "$val"
}

# Same fallback chain gen-db-provision.sh uses: bc.qualifier from
# application.properties, else the directory name with "bc-" stripped.
resolve_qualifier() {
    local bc="$1" props="$2"
    local qualifier=""
    if [[ -n "$props" ]]; then
        qualifier="$(extract_default "$props" "bc.qualifier")"
    fi
    if [[ -z "$qualifier" ]]; then
        qualifier="${bc#bc-}"
    fi
    echo "$qualifier"
}

extract_default_port() {
    local props="$1"
    [[ -n "${props:-}" && -f "$props" ]] || return 0
    local line port
    line="$(grep -E '^[[:space:]]*server\.port=' "$props" | head -n 1 | tr -d '\r' || true)"
    [[ -n "${line:-}" ]] || return 0
    port="$(echo "$line" | sed -nE 's/.*\$\{SERVER_PORT:([0-9]+)\}.*/\1/p')"
    [[ -n "${port:-}" && "$port" =~ ^[0-9]+$ ]] && echo "$port"
}

# Returns 0 (true) if the BC contains any @Entity annotation in its Java sources
bc_has_entities() {
    local bc_dir="$1"
    grep -rl "@Entity" "$bc_dir" \
        --include="*.java" \
        --exclude-dir=target \
        --exclude-dir=".git" \
        2>/dev/null | grep -q .
}

# ── Compose header ─────────────────────────────────────────────────────────────

cat <<YAML
services:
YAML

# ── BC services ───────────────────────────────────────────────────────────────
#
# Internal-only: expose (not ports). Nothing outside the Docker network
# talks to a BC directly — only Gateways are reachable from the host,
# per the platform's own design ("no ports exposed except through
# Gateways").

for bc in bc-*; do
    [[ -d "$bc" ]] || continue

    dockerfile_path="$(find "$bc" -maxdepth "$SEARCH_DEPTH" -type f -name Dockerfile | sort | head -n 1 || true)"
    [[ -n "$dockerfile_path" ]] || continue

    svc="$(service_name_from_bc "$bc")"
    props_path="$(find_app_props "$bc")"
    default_port="$(extract_default_port "$props_path" || true)"

    has_db=false
    if bc_has_entities "$bc"; then
        has_db=true
    fi

    cat <<YAML

  ${svc}:
    build:
      context: .
      dockerfile: ${dockerfile_path}
    image: "${REGISTRY_IMAGE}/${svc}-service:${IMAGE_TAG}"
    volumes:
      - "\${CERTS_DIR:-${CERTS_DIR}}:/tls-src:ro"
YAML

    if $has_db; then
        qualifier="$(resolve_qualifier "$bc" "$props_path")"
        db_name="${PROJECT_NAME}_${qualifier}_db"
        db_user="$qualifier"
        db_pass="${qualifier}_${DB_PASSWORD_SUFFIX}"
        cat <<YAML
    environment:
      - DB_HOST=\${DB_HOST}
      - DB_PORT=\${DB_PORT}
      - DB_NAME=${db_name}
      - DB_USERNAME=${db_user}
      - DB_PASSWORD=${db_pass}
      - SPRING_JPA_ENABLED=true
YAML
    fi

    if [[ -n "${default_port:-}" ]]; then
        cat <<YAML
    expose:
      - "${default_port}"
YAML
    fi

    cat <<YAML
    networks:
      default:
        aliases:
          - "bc-${svc}.sop.local"
YAML
done

# ── Gateway services ─────────────────────────────────────────────────────────
#
# The only services published to the host — ports (not expose). This
# is where a browser/nginx or a local `curl --cert` reaches the mesh
# from outside; every BC stays unreachable except through here.
# Single-module (no -api/-impl split, see gen-gateway-dockerfiles.sh),
# so dockerfile_path is just ${gw}/Dockerfile rather than a nested find.
#
# Host port is overridable per-gateway via <GW_NAME>_PORT (dashes ->
# underscores, e.g. GW_ADMIN_PORT), so two gateways with a colliding
# default port can still both run without editing the compose file.

for gw in gw-*; do
    [[ -d "$gw" ]] || continue

    dockerfile_path="${gw}/Dockerfile"
    [[ -f "$dockerfile_path" ]] || continue

    props_path="$(find_app_props "$gw")"
    default_port="$(extract_default_port "$props_path" || true)"

    cat <<YAML

  ${gw}:
    build:
      context: .
      dockerfile: ${dockerfile_path}
    image: "${REGISTRY_IMAGE}/${gw}-service:${IMAGE_TAG}"
    volumes:
      - "\${CERTS_DIR:-${CERTS_DIR}}:/tls-src:ro"
YAML

    if [[ -n "${default_port:-}" ]]; then
        env_var="$(echo "${gw}_PORT" | tr '[:lower:]-' '[:upper:]_')"
        cat <<YAML
    ports:
      - "\${${env_var}:-${default_port}}:${default_port}"
YAML
    fi

    cat <<YAML
    networks:
      default:
        aliases:
          - "${gw}.sop.local"
YAML
done

cat <<YAML

networks:
  default:
    name: "\${COMPOSE_PROJECT_NAME:-sop}-net"
YAML