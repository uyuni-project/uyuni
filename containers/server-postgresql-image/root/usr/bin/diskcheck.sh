#!/bin/bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

# Exit with the severity: 0 OK, 2 alert, 3 critical.
# *_FREE_MB, when set, has priority over the percentage threshold of the same level.

ALERT=${DISKCHECKALERT:-90}
THRESHOLD=${DISKTHRESHOLD:-95}
PGDATA="${PGDATA:-/var/lib/pgsql/data}"

# No awk in this image
read -r FREE_MB DISK_USAGE <<< "$(df --output=avail,pcent -B1M "$PGDATA" | tail -1 | tr -d '%')"

SEVERITY=0
if [ -n "$DISKCHECKALERT_FREE_MB" ]; then
    [ "$FREE_MB" -lt "$DISKCHECKALERT_FREE_MB" ] && SEVERITY=2
elif [ "$DISK_USAGE" -gt "$ALERT" ]; then
    SEVERITY=2
fi
if [ -n "$DISKTHRESHOLD_FREE_MB" ]; then
    [ "$FREE_MB" -lt "$DISKTHRESHOLD_FREE_MB" ] && SEVERITY=3
elif [ "$DISK_USAGE" -gt "$THRESHOLD" ]; then
    SEVERITY=3
fi

echo "Disk usage is at ${DISK_USAGE}% (${FREE_MB} MB available)."
exit "$SEVERITY"
