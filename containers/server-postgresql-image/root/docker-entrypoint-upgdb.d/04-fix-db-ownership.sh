#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only
#
# Ensure the manager and report databases are owned by their respective users.
#
# Installations created before May 2025 (commit 7f67dc7) had both databases
# owned by the PostgreSQL superuser because the initdb scripts did not set
# ownership. get_manager_db_name() in uyuni-helpers.sh relies on ownership to
# locate the database, so this script must run before any upgrade script that
# calls it.
#
# Note: SQL is passed to psql on stdin (not via -c) so that variable
# interpolation (:'var') and \gexec work.

set -euo pipefail

run_sql() {
    PGHOST='' PGHOSTADDR='' psql -v ON_ERROR_STOP=1 \
        -p "${PGPORT:-5432}" \
        -U "${POSTGRES_USER:-postgres}" \
        --no-password --no-psqlrc -d postgres "$@"
}

# Returns 0 if the database exists, 1 otherwise.
db_exists() {
    local db_name="$1"
    local found
    found=$(run_sql -v db_name="$db_name" -t -A \
        <<< "SELECT 1 FROM pg_database WHERE datname = :'db_name';") || {
        echo "Failed to query pg_database while checking for '$db_name'." >&2
        exit 1
    }
    [ "$found" = "1" ]
}

# fix_ownership <db_name> <expected_owner>
# Assumes the database exists (callers check with db_exists).
fix_ownership() {
    local db_name="$1"
    local expected_owner="$2"

    if [ -z "$expected_owner" ]; then
        echo "Owner for '$db_name' is not set, skipping."
        return 0
    fi

    local current_owner
    current_owner=$(run_sql -v db_name="$db_name" -t -A <<< \
        "SELECT r.rolname FROM pg_database d JOIN pg_roles r ON d.datdba = r.oid WHERE d.datname = :'db_name';")

    if [ "$current_owner" = "$expected_owner" ]; then
        echo "Database '$db_name' is already owned by '$expected_owner', nothing to do."
        return 0
    fi

    echo "Fixing ownership of '$db_name': '$current_owner' -> '$expected_owner'"

    # format('%I', ...) safely quotes identifiers; \gexec runs the result.
    run_sql -v db="$db_name" -v owner="$expected_owner" <<< \
        "SELECT format('ALTER DATABASE %I OWNER TO %I;', :'db', :'owner') \gexec"

    echo "Ownership of '$db_name' transferred to '$expected_owner'."
}

# Main manager database — probe by well-known names since ownership may be wrong.
DB_NAME=""
for name in susemanager uyuni; do
    if db_exists "$name"; then
        DB_NAME="$name"
        break
    fi
done

if [ -z "$DB_NAME" ]; then
    echo "Could not find manager database (susemanager or uyuni), skipping."
else
    fix_ownership "$DB_NAME" "${MANAGER_USER:-}"
fi

# Report database — name is fixed, owner is REPORT_DB_USER.
if db_exists "reportdb"; then
    fix_ownership "reportdb" "${REPORT_DB_USER:-}"
else
    echo "Database 'reportdb' does not exist, skipping."
fi
