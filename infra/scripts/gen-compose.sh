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

# ── helpers ────────────────────────────────────────────────────────────────────

service_name_from_bc() {
    basename "$1" | sed 's/^bc-//'
}

find_app_props() {
    local bc_dir="$1"
    find "$bc_dir" -maxdepth 6 -type f -name application.properties 2>/dev/null | sort | head -n 1 || true
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
        cat <<YAML
    environment:
      - DB_HOST=\${DB_HOST}
      - DB_PORT=\${DB_PORT}
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

cat <<YAML

networks:
  default:
    name: "\${COMPOSE_PROJECT_NAME:-sop}-net"
YAML