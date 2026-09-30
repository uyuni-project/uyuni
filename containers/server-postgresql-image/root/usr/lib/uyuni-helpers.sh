#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

get_manager_db_name() {
    if [ -z "${MANAGER_USER}" ]; then
        echo "Error: MANAGER_USER environment variable is not specified." >&2
        return 1
    fi

    # Retrieve all databases owned by the MANAGER_USER.
    # We connect using -d postgres which is guaranteed to exist.
    local db_names
    db_names=$(PGHOST='' PGHOSTADDR='' psql -v ON_ERROR_STOP=1 \
        -p "${PGPORT:-5432}" \
        -U "${POSTGRES_USER:-postgres}" \
        --no-password --no-psqlrc -d postgres \
        -t -A \
        -c "SELECT d.datname FROM pg_database d JOIN pg_roles r ON d.datdba = r.oid WHERE r.rolname = '${MANAGER_USER}';")

    local exit_code=$?
    if [ $exit_code -ne 0 ]; then
        echo "Error: Failed to query database for user '${MANAGER_USER}' (exit code: $exit_code)." >&2
        return 1
    fi

    local count
    count=$(echo "$db_names" | grep -cv '^$')

    if [ "$count" -eq 0 ]; then
        echo "Error: No database owned by user '${MANAGER_USER}' was found." >&2
        return 1
    elif [ "$count" -gt 1 ]; then
        echo "Error: More than one database owned by user '${MANAGER_USER}' was found: $(echo "$db_names" | tr '\n' ' ')" >&2
        return 1
    fi

    echo "$db_names"
}
