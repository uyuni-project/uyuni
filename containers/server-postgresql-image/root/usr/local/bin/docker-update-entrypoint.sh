#!/usr/bin/env bash
set -Eeo pipefail
shopt -s nullglob

export POSTGRESQL=/var/lib/pgsql/data/data/postgresql.conf
chmod +x /usr/local/bin/docker-entrypoint.sh
source /usr/local/bin/docker-entrypoint.sh
chmod +x /usr/local/bin/postgres_utils.sh
source /usr/local/bin/postgres_utils.sh


# Disable SSL, it will allow to run pg without certificate
postgres_reconfig "ssl" "off"

# Start PostgreSQL in the background
su - postgres -c "postgres -D /var/lib/pgsql/data/data" &

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to start..."
sleep 1

echo "PostgreSQL is up. Running init scripts..."

# Run SQL initialization scripts: the zz* requires postgres up and running
# Apply them before changes on SSL
for f in /docker-entrypoint-initdb.d/zz*.sql; do
    echo "Running $f..."
    psql -U postgres -f "$f" || { echo "Error executing $f"; exit 1; }
done

# Now all the others settings can be enabled.
exec /docker-entrypoint-initdb.d/uyuni-postgres-config.sh
