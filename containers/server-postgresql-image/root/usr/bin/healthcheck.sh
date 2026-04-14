#!/bin/bash
set -e

# Set threshold to env var DISKTHRESHOLD, default to 95 if not set
THRESHOLD=${DISKTHRESHOLD:-95}
PGUSER=${POSTGRES_USER:-postgres}
PGPORT=${POSTGRES_PORT:-5432}

# Get current disk usage percentage
DISK_USAGE=$(df --output=pcent /var/lib/pgsql/data | tail -1 | tr -d '%')

if [ "$DISK_USAGE" -gt "$THRESHOLD" ]; then
    echo "Healthcheck failed: Disk usage is at ${DISK_USAGE}%, which exceeds the ${THRESHOLD}% threshold."
    exit 1
fi

if ! pg_isready -U "$PGUSER" -h localhost -p "$PGPORT" > /dev/null; then
    echo "Healthcheck failed: PostgreSQL is not ready (user: ${PGUSER}, host: localhost, port: ${PGPORT})."
    exit 1
fi

echo "Healthcheck passed: Disk usage is at ${DISK_USAGE}% (Threshold: ${THRESHOLD}%), PostgreSQL is ready."
exit 0
