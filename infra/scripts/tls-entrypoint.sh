#!/usr/bin/env bash
set -euo pipefail

TLS_SRC="${TLS_SRC:-/tls-src}"
TLS_DIR="${TLS_DIR:-/tls}"
svc="${SERVICE_NAME:-}"
svc_safe="${svc//-/_}"

if [ -z "$svc" ]; then
    echo "SERVICE_NAME not set"
    exit 1
fi

mkdir -p "$TLS_DIR"

tls_key="$svc"
if [ "$svc" = "audit-log" ]; then
    tls_key="audit_log"
fi
tls_key_safe="${tls_key//-/_}"

# Look for both dash and underscore variants, flat and nested
candidates="$TLS_SRC/${tls_key}.p12 $TLS_SRC/${tls_key_safe}.p12 $TLS_SRC/${svc}.p12 $TLS_SRC/${svc_safe}.p12"
candidates="$candidates $TLS_SRC/${tls_key}/${tls_key}.p12 $TLS_SRC/${tls_key_safe}/${tls_key_safe}.p12 $TLS_SRC/${svc}/${svc}.p12 $TLS_SRC/${svc_safe}/${svc_safe}.p12"

found=""
for f in $candidates; do
    if [ -f "$f" ]; then
        found="$f"
        break
    fi
done

if [ -z "$found" ]; then
    echo "TLS keystore not found for service '$svc'. Looked for: $candidates"
    echo "Current content of $TLS_SRC:"
    ls -la "$TLS_SRC" || echo "Directory $TLS_SRC empty or missing"
    exit 1
fi

# Create symlinks for both dash and underscore forms so app configs can use either
for linkname in "$tls_key" "$tls_key_safe" "$svc" "$svc_safe"; do
    target="$TLS_DIR/${linkname}.p12"
    [ -e "$target" ] || ln -s "$found" "$target"
done

# Optional shared files
for extra in internal-ca-trust.p12 ca-cert.pem client-cert.pem client-key.pem; do
    if [ -f "$TLS_SRC/$extra" ] && [ ! -e "$TLS_DIR/$extra" ]; then
        ln -s "$TLS_SRC/$extra" "$TLS_DIR/$extra"
    fi
done

echo "TLS ready for $svc"
ls -la "$TLS_DIR" || true
exec "$@"