#!/bin/bash
set -e

PGUSER=${POSTGRES_USER:-postgres}
PGPORT=${POSTGRES_PORT:-5432}

UPGRADE_IN_PROGRESS="/run/postgresql/upgrade_in_progress"

DISK_SEVERITY=0
/usr/bin/diskcheck.sh || DISK_SEVERITY=$?
if [ "$DISK_SEVERITY" -eq 3 ]; then
    exit 1
fi

if [ -f "$UPGRADE_IN_PROGRESS" ]; then
    echo "Healthcheck failed: Database upgrade is currently in progress."
    exit 1
fi

if ! pg_isready -U "$PGUSER" -h localhost -p "$PGPORT" > /dev/null; then
    echo "Healthcheck failed: PostgreSQL is not ready (user: ${PGUSER}, host: localhost, port: ${PGPORT})."
    exit 1
fi

echo "Healthcheck passed: PostgreSQL is ready."
exit 0
