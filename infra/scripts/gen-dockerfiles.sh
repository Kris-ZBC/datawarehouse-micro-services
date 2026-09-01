#!/usr/bin/env bash
set -euo pipefail

# Usage: bash infra/scripts/gen-dockerfiles.sh
# Run from $PROJECT_ROOT.
#
# Scans for microservices by looking for bc-* directories that contain
# both an *-api and *-impl subdirectory. For each one found, it writes
# a self-contained Dockerfile into the *-impl directory.
#
# The only thing this script cannot derive automatically is the port,
# so it reads that from the service's application.properties.

extract_port() {
    local impl_dir="$1"
    local props
    props="$(find "$impl_dir" -maxdepth 6 -type f -name application.properties 2>/dev/null | sort | head -n 1 || true)"
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

found=0

for bc_dir in bc-*/; do
    [[ -d "$bc_dir" ]] || continue

    bc_name="${bc_dir%/}"           # e.g. bc-apprentice
    svc_name="${bc_name#bc-}"       # e.g. apprentice

    # Check for both -api and -impl subdirectories
    api_dir="$(find "$bc_dir" -maxdepth 1 -type d -name "*-api" | head -n 1 || true)"
    impl_dir="$(find "$bc_dir" -maxdepth 1 -type d -name "*-impl" | head -n 1 || true)"

    if [[ -z "$api_dir" || -z "$impl_dir" ]]; then
        echo "⚠  Skipping $bc_name — missing -api or -impl directory"
        continue
    fi

    impl_basename="$(basename "$impl_dir")"   # e.g. apprentice-impl
    bc_qualifier="${svc_name//-/_}"           # e.g. consent_saga
    cert_filename="${svc_name}.p12"           # e.g. consent-saga.p12
    port="$(extract_port "$impl_dir")"

    if [[ "$port" == "UNKNOWN" ]]; then
        echo "⚠  $bc_name — could not determine port from application.properties, defaulting to UNKNOWN. Set SERVER_PORT manually."
    fi

    dockerfile="$impl_dir/Dockerfile"

    cat > "$dockerfile" <<EOF
FROM eclipse-temurin:21-jre
WORKDIR /app

ARG JAR_FILE=${bc_name}/${impl_basename}/target/${impl_basename}-*.jar
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

    echo "✔  $dockerfile (port: $port)"
    found=$((found + 1))
done

echo ""
echo "Done — ${found} Dockerfile(s) generated."