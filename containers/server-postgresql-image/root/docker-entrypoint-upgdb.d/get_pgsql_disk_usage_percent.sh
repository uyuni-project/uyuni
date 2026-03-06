#!/bin/bash
# SPDX-FileCopyrightText: 2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

# Set thresholds from env vars or defaults
DB_CRIT=${DISKTHRESHOLD:-95}
DB_WARN=${DISKCHECKALERT:-90}

run_sql() {
    psql -v ON_ERROR_STOP=1 \
        -h localhost \
        -p "${PGPORT:-5432}" \
        -U "$POSTGRES_USER" \
        --no-password --no-psqlrc -d susemanager "$@"
}

cat << EOF | run_sql
CREATE OR REPLACE FUNCTION get_pgsql_disk_usage_percent()
RETURNS integer
SECURITY DEFINER
AS
\$\$
DECLARE
    raw_output text;
    usage_pct integer;
BEGIN
    CREATE TEMP TABLE IF NOT EXISTS tmp_sys_df (content text) ON COMMIT DROP;
    TRUNCATE tmp_sys_df;

    COPY tmp_sys_df FROM PROGRAM 'df --output=pcent /var/lib/pgsql/data/ | tail -1';
    SELECT content INTO raw_output FROM tmp_sys_df;
    
    usage_pct := trim(both ' %' from raw_output)::integer;

    RETURN CASE
        WHEN usage_pct >= $DB_CRIT THEN 3
        WHEN usage_pct >= $DB_WARN THEN 2
        ELSE 0
    END;
EXCEPTION 
    WHEN OTHERS THEN 
        RAISE WARNING 'Disk usage check failed. Error: % (SQLSTATE: %)', SQLERRM, SQLSTATE;
        
        RETURN -1;
END;
\$\$ LANGUAGE plpgsql;
EOF
