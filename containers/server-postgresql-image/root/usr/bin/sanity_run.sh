#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: MIT

# Sanity checks run as root before privilege drop.
# Fixes ownership/permission issues from legacy installations.

set -Eeo pipefail

PGDATA="${PGDATA:-/var/lib/pgsql/data}"

log() {
    echo "[SANITY] $*" >&2
}

# Nothing to do on fresh installs — upstream entrypoint handles those
if [ ! -d "$PGDATA" ]; then
    log "PGDATA does not exist, skipping sanity checks."
    exit 0
fi

POSTGRES_UID=$(id -u postgres)

# Check and fix ownerships
data_owner=$(stat -c '%u' "$PGDATA")
if [ "$data_owner" != "$POSTGRES_UID" ]; then
    log "PGDATA owned by UID $data_owner, fixing recursively..."
    chown -R postgres:postgres "$PGDATA"
else
    # Targeted fix for config files known to be left root-owned on upgrades
    for f in pg_hba.conf postgresql.conf pg_ident.conf; do
        fpath="$PGDATA/$f"
        [ -f "$fpath" ] || continue
        fowner=$(stat -c '%u' "$fpath")
        if [ "$fowner" != "$POSTGRES_UID" ]; then
            log "$f owned by UID $fowner, fixing ownership..."
            chown postgres:postgres "$fpath"
        fi
    done
fi

# Check and fix permissions
for f in pg_hba.conf postgresql.conf pg_ident.conf; do
    fpath="$PGDATA/$f"
    [ -f "$fpath" ] || continue
    perms=$(stat -c '%a' "$fpath")
    if [ "$perms" != "600" ]; then
        log "$f has permissions $perms, setting to 0600..."
        chmod 0600 "$fpath"
    fi
done

# Fix /run/postgresql ownership, which is inherited from BCI base
if [ ! -d /run/postgresql ]; then
    log "/run/postgresql missing, creating..."
    mkdir -p /run/postgresql
fi
rundir_owner=$(stat -c '%u' /run/postgresql)
if [ "$rundir_owner" != "$POSTGRES_UID" ]; then
    log "/run/postgresql owned by UID $rundir_owner, fixing..."
    chown postgres:postgres /run/postgresql
fi

log "Sanity checks complete."
