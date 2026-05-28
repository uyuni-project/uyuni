#!/bin/bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

function run_sql() {
    PGHOST='' PGHOSTADDR='' psql -v ON_ERROR_STOP=1 \
        -p "${PGPORT:-5432}" \
        -U postgres \
        --no-password --no-psqlrc --tuples-only --no-align "$@"
}

function query_collation_difference() {
    cat << EOF | run_sql
SELECT COALESCE(
    (SELECT d.datcollversion IS DISTINCT FROM pg_database_collation_actual_version(d.oid)
      FROM pg_database as d
     WHERE d.datname = '$1'
), false);
EOF
}

# Check and fix collation differences in system tables
for dbname in postgres template1; do
    RES=$(query_collation_difference $dbname)

    if [ "$RES" = "t" ]; then
        echo "ALTER DATABASE $dbname REFRESH COLLATION VERSION;" | run_sql
    fi
done
