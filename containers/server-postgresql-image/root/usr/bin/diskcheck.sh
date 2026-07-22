#!/bin/bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

THRESHOLD=${DISKTHRESHOLD:-95}
PGDATA="${PGDATA:-/var/lib/pgsql/data}"

DISK_USAGE=$(df --output=pcent "$PGDATA" | tail -1 | tr -d '%')

if [ "$DISK_USAGE" -gt "$THRESHOLD" ]; then
    echo "Disk usage is at ${DISK_USAGE}%, which exceeds the ${THRESHOLD}% threshold."
    exit 1
fi

echo "Disk usage is at ${DISK_USAGE}% (Threshold: ${THRESHOLD}%)."
exit 0
