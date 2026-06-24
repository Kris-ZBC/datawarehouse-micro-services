#!/usr/bin/env sh
set -e

# Use variables passed from docker-compose
DB_HOST="${DB_HOST:-dwhpoc.sop.local}"
DB_PORT="${DB_PORT:-3306}"
#has to be the backend_prog_user password not root password
ROOT_PW="${MARIADB_ROOT_PASSWORD}"
SQL_FILE="${SQL_FILE:-/provision/00-create-dbs.generated.sql}"

echo "Attempting to provision external database at $DB_HOST:$DB_PORT"

# Run the command ONCE. 
# We use --ssl and --ssl-verify-server-cert=0 to skip certificate chain 
# validation while still using an encrypted tunnel.
mariadb \
  -h "$DB_HOST" \
  -P "$DB_PORT" \
  -u backend_prog_user \
  -p"$ROOT_PW" \
  --ssl \
  --ssl-verify-server-cert=0 \
  < "$SQL_FILE"

echo "Success: Database provisioning complete."