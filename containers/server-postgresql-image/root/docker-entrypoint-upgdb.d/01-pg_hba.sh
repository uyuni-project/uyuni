#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-Only

# we need to make sure there is correct access for our services

PGDATA="${PGDATA:-/var/lib/pgsql/data}"
HBA_FILE="${PGDATA}/pg_hba.conf"
PG_HBA_CHANGED=""

if [ -z "$(sed -n '/^local all all peer$/p' "$HBA_FILE")" ]; then
    echo "local all all peer" >> "$HBA_FILE"
    PG_HBA_CHANGED="y"
fi

if [ -z "$(sed -n '/^local replication all peer$/p' "$HBA_FILE")" ]; then
    echo "local replication all peer" >> "$HBA_FILE"
    PG_HBA_CHANGED="y"
fi

if [ -z "$(sed -n '/^host all all all scram-sha-256$/p' "$HBA_FILE")" ]; then
    echo "host all all all scram-sha-256" >> "$HBA_FILE"
    PG_HBA_CHANGED="y"
fi

# restart database to pickup new pg_hba changes
if [ -n "$PG_HBA_CHANGED" ]; then
    pg_ctl -D "$PGDATA" reload
fi