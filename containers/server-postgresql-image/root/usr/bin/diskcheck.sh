#!/bin/bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

# Exit with the severity: 0 OK, 1 misconfiguration, 2 alert, 3 critical.
# *_FREE_MB, when set, has priority over the percentage threshold of the same level.

DISKCHECKALERT=${DISKCHECKALERT:-90}
DISKTHRESHOLD=${DISKTHRESHOLD:-95}
PGDATA="${PGDATA:-/var/lib/pgsql/data}"

SEVERITY=0

# Reset the named variable to the default if it is not a percentage between 0 and 100,
# and report the misconfiguration.
validate_percentage() {
    local name="$1"
    local default="$2"
    local value="${!name}"

    if ! [[ "$value" =~ ^[0-9]{1,3}$ ]] || [ "$value" -gt 100 ]; then
        echo "Invalid $name value '$value', using the default ${default}%."
        printf -v "$name" '%s' "$default"
        SEVERITY=1
    fi
}

# Unset the named variable if it is not a valid number of MB, so that the percentage
# threshold of the same level is used, and report the misconfiguration.
validate_free_mb() {
    local name="$1"
    local value="${!name}"

    if [ -n "$value" ] && ! [[ "$value" =~ ^[0-9]{1,15}$ ]]; then
        echo "Invalid $name value '$value', using the percentage threshold."
        unset "$name"
        SEVERITY=1
    fi
}

validate_percentage DISKCHECKALERT 90
validate_percentage DISKTHRESHOLD 95
validate_free_mb DISKCHECKALERT_FREE_MB
validate_free_mb DISKTHRESHOLD_FREE_MB

# No awk in this image
read -r FREE_MB DISK_USAGE <<< "$(df --output=avail,pcent -B1M "$PGDATA" | tail -1 | tr -d '%')"

if [ -n "$DISKCHECKALERT_FREE_MB" ]; then
    [ "$FREE_MB" -lt "$DISKCHECKALERT_FREE_MB" ] && SEVERITY=2
elif [ "$DISK_USAGE" -gt "$DISKCHECKALERT" ]; then
    SEVERITY=2
fi
if [ -n "$DISKTHRESHOLD_FREE_MB" ]; then
    [ "$FREE_MB" -lt "$DISKTHRESHOLD_FREE_MB" ] && SEVERITY=3
elif [ "$DISK_USAGE" -gt "$DISKTHRESHOLD" ]; then
    SEVERITY=3
fi

echo "Disk usage is at ${DISK_USAGE}% (${FREE_MB} MB available)."
exit "$SEVERITY"
