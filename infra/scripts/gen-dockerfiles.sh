#!/usr/bin/env bash
set -euo pipefail

# Usage: bash infra/scripts/gen-dockerfiles.sh
# Run from $PROJECT_ROOT.
#
# Generates Dockerfiles for both service families in this platform:
#   bc-*  — hexagonal split (sibling -api/-impl dirs), Dockerfile goes
#           in the -impl module, includes DB_HOST/DB_PORT env vars.
#   gw-*  — single module (no -api/-impl split), Dockerfile goes at
#           the module root, no DB env vars (gateways are stateless).
# Merged from what used to be gen-dockerfiles.sh (bc-*) and
# gen-gateway-dockerfiles.sh (gw-*) — the detection logic genuinely
# differs between the two families (structural check vs single-module),
# so they stay as two clearly separated blocks rather than one
# generalized loop that would just reintroduce the -api/-impl
# assumption gateways don't meet.

# ── shared helpers ─────────────────────────────────────────────────────────────

extract_port() {
    local module_dir="$1"
    local props
    props="$(find "$module_dir" -maxdepth 6 -type f -name application.properties 2>/dev/null | sort | head -n 1 || true)"
    if [[ -n "$props" && -f "$props" ]]; then
        local port
        port="$(grep -E '^[[:space:]]*server\.port=' "$props" | head -n 1 | tr -d '\r' | sed -nE 's/.*\$\{SERVER_PORT:([0-9]+)\}.*/\1/p' || true)"
        if [[ -n "$port" && "$port" =~ ^[0-9]+$ ]]; then
            echo "$port"
            return
        fi
    fi
    echo "UNKNOWN"
}

# Reads the Maven artifactId straight from the module's own pom.xml
# rather than assuming it matches the directory name — that assumption
# is confirmed false for gateways (gw-admin dir, gateway.admin
# artifactId) and unverified for BCs, so both blocks use this. Strips
# the <parent> block first, otherwise this picks up the PARENT's
# artifactId instead of the module's own (parent's <artifactId> tag
# appears first in the file).
extract_artifact_id() {
    local module_dir="$1"
    sed '/<parent>/,/<\/parent>/d' "${module_dir}/pom.xml" \
        | grep -m1 "<artifactId>" \
        | sed -nE 's#.*<artifactId>(.*)</artifactId>.*#\1#p'
}

bc_found=0
gw_found=0

# ── bc-* services ────────────────────────────────────────────────────────────
#
# Scans for bc-* directories containing both an *-api and *-impl
# subdirectory. The only thing this can't derive automatically is the
# port, so it reads that from the service's application.properties.

for bc_dir in bc-*/; do
    [[ -d "$bc_dir" ]] || continue

    bc_name="${bc_dir%/}"           # e.g. bc-apprentice
    svc_name="${bc_name#bc-}"       # e.g. apprentice

    api_dir="$(find "$bc_dir" -maxdepth 1 -type d -name "*-api" | head -n 1 || true)"
    impl_dir="$(find "$bc_dir" -maxdepth 1 -type d -name "*-impl" | head -n 1 || true)"

    if [[ -z "$api_dir" || -z "$impl_dir" ]]; then
        echo "⚠  Skipping $bc_name — missing -api or -impl directory"
        continue
    fi

    impl_dir="${impl_dir%/}"
    bc_qualifier="${svc_name//-/_}"           # e.g. consent_saga
    cert_filename="${svc_name}.p12"           # e.g. consent-saga.p12
    artifact_id="$(extract_artifact_id "$impl_dir")"
    port="$(extract_port "$impl_dir")"

    if [[ -z "$artifact_id" ]]; then
        echo "⚠  $bc_name — could not read artifactId from ${impl_dir}/pom.xml, skipping"
        continue
    fi

    if [[ "$port" == "UNKNOWN" ]]; then
        echo "⚠  $bc_name — could not determine port from application.properties, defaulting to UNKNOWN. Set SERVER_PORT manually."
    fi

    dockerfile="$impl_dir/Dockerfile"

    cat > "$dockerfile" <<EOF
FROM eclipse-temurin:21-jre
WORKDIR /app

ARG JAR_FILE=${impl_dir}/target/${artifact_id}-*.jar
COPY \${JAR_FILE} app.jar

COPY infra/scripts/tls-entrypoint.sh /tls-entrypoint.sh
RUN chmod +x /tls-entrypoint.sh

RUN mkdir -p /tls /tls-src

ENV BC_QUALIFIER=${bc_qualifier}
ENV SERVICE_NAME=${svc_name}
ENV CERTIFICATE_FILENAME=${cert_filename}
ENV DB_HOST=${DB_HOST:-dwhpoc.sop.local}
ENV TLS_DIR=${TLS_DIR:-/tls}
ENV TLS_SRC=${TLS_SRC:-/tls-src}
ENV DB_PORT=${DB_PORT:-3306}
ENV JAVA_OPTS=""

EXPOSE ${port}

ENTRYPOINT ["/tls-entrypoint.sh", "bash", "-c", "java \$JAVA_OPTS -jar /app/app.jar"]
EOF

    echo "✔  $dockerfile (artifact: $artifact_id, port: $port)"
    bc_found=$((bc_found + 1))
done

# ── gw-* services ────────────────────────────────────────────────────────────
#
# Single module — no -api/-impl split. Naming: gw-admin's artifactId is
# "gateway.admin" (not "gw-admin"), its bc.qualifier default is
# "gateway_admin", and its CERTIFICATE_FILENAME default is
# "gateway-admin.p12" — three different separators for the same base
# name, all already fixed by each gateway's own application.properties,
# not invented here. service_name below ("gateway-admin") must match
# CERTIFICATE_FILENAME exactly, since that's what tls-entrypoint.sh's
# SERVICE_NAME-based lookup searches CERTS_DIR for. No DB env vars —
# gateways are stateless routers.

for gw_dir in gw-*/; do
    [[ -d "$gw_dir" ]] || continue
    [[ -f "${gw_dir}pom.xml" ]] || continue

    gw_name="${gw_dir%/}"                    # e.g. gw-admin
    svc_name="${gw_name#gw-}"                # e.g. admin
    bc_qualifier="gateway_${svc_name}"       # matches bc.qualifier default
    cert_filename="gateway-${svc_name}.p12"  # matches CERTIFICATE_FILENAME default
    service_name="gateway-${svc_name}"       # what tls-entrypoint.sh looks up in CERTS_DIR
    artifact_id="$(extract_artifact_id "$gw_name")"
    port="$(extract_port "$gw_dir")"

    if [[ -z "$artifact_id" ]]; then
        echo "⚠  $gw_name — could not read artifactId from pom.xml, skipping"
        continue
    fi

    if [[ "$port" == "UNKNOWN" ]]; then
        echo "⚠  $gw_name — could not determine port from application.properties, defaulting to UNKNOWN. Set SERVER_PORT manually."
    fi

    dockerfile="${gw_dir}Dockerfile"

    cat > "$dockerfile" <<EOF
FROM eclipse-temurin:21-jre
WORKDIR /app

ARG JAR_FILE=${gw_name}/target/${artifact_id}-*.jar
COPY \${JAR_FILE} app.jar

COPY infra/scripts/tls-entrypoint.sh /tls-entrypoint.sh
RUN chmod +x /tls-entrypoint.sh

RUN mkdir -p /tls /tls-src

ENV BC_QUALIFIER=${bc_qualifier}
ENV SERVICE_NAME=${service_name}
ENV CERTIFICATE_FILENAME=${cert_filename}
ENV TLS_DIR=${TLS_DIR:-/tls}
ENV TLS_SRC=${TLS_SRC:-/tls-src}
ENV JAVA_OPTS=""

EXPOSE ${port}

ENTRYPOINT ["/tls-entrypoint.sh", "bash", "-c", "java \$JAVA_OPTS -jar /app/app.jar"]
EOF

    echo "✔  $dockerfile (artifact: $artifact_id, port: $port)"
    gw_found=$((gw_found + 1))
done

echo ""
echo "Done — ${bc_found} BC Dockerfile(s), ${gw_found} gateway Dockerfile(s) generated."