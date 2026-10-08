#!/bin/bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

# The thresholds are not stored in the function: /usr/bin/diskcheck.sh reads them
# from the environment of the PostgreSQL server at every call.

. /usr/lib/uyuni-helpers.sh
MANAGER_DB_NAME=$(get_manager_db_name) || exit 1

run_sql() {
    PGHOST='' PGHOSTADDR='' psql -v ON_ERROR_STOP=1 \
        -p "${PGPORT:-5432}" \
        -U "$POSTGRES_USER" \
        --no-password --no-psqlrc -d "$MANAGER_DB_NAME"
}

cat << EOF | run_sql
CREATE OR REPLACE FUNCTION get_pgsql_disk_severity()
RETURNS integer
SECURITY DEFINER
AS
\$\$
DECLARE
    raw_output text;
BEGIN
    CREATE TEMP TABLE IF NOT EXISTS tmp_sys_df (content text) ON COMMIT DROP;
    TRUNCATE tmp_sys_df;

    COPY tmp_sys_df FROM PROGRAM '/usr/bin/diskcheck.sh >/dev/null 2>&1; echo \$?';
    SELECT content INTO raw_output FROM tmp_sys_df;

    RETURN trim(raw_output)::integer;
EXCEPTION
    WHEN OTHERS THEN
        RAISE WARNING 'Disk usage check failed. Error: % (SQLSTATE: %)', SQLERRM, SQLSTATE;

        RETURN -1;
END;
\$\$ LANGUAGE plpgsql;
EOF
