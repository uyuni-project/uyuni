#!/usr/bin/env bash
set -Eeo pipefail

declare -g DATABASE_ALREADY_EXISTS
: "${DATABASE_ALREADY_EXISTS:=}"
# look specifically for PG_VERSION, as it is expected in the DB dir
PGDATA="/var/lib/pgsql/data/"
if [ -s "$PGDATA/PG_VERSION" ]; then
        DATABASE_ALREADY_EXISTS='true'
fi

if [ -z "$DATABASE_ALREADY_EXISTS" ]; then
        chmod +x /usr/local/bin/docker-update-entrypoint.sh
        /usr/local/bin/docker-update-entrypoint.sh "$@"
fi

export POSTGRESQL=/var/lib/pgsql/data/postgresql.conf
exec /usr/local/bin/docker-entrypoint.sh "$@"
