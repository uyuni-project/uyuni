--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE OR REPLACE FUNCTION get_pgsql_disk_usage_percent()
RETURNS integer
SECURITY DEFINER
AS
$$
DECLARE
    raw_output text;
    usage_pct integer;
BEGIN
    CREATE TEMP TABLE IF NOT EXISTS tmp_sys_df (content text) ON COMMIT DROP;
    TRUNCATE tmp_sys_df;
    
    -- Hardcoded command. No user input can be injected here.
    COPY tmp_sys_df FROM PROGRAM 'df --output=pcent /var/lib/pgsql/data/ | tail -1';
    SELECT content INTO raw_output FROM tmp_sys_df;
    
    usage_pct := trim(both ' %' from raw_output)::integer;

    RETURN CASE
        WHEN usage_pct >= 95 THEN 3  -- CRITICAL
        WHEN usage_pct >= 90 THEN 2  -- ALERT
        ELSE 0                       -- OK
    END;
EXCEPTION 
    WHEN OTHERS THEN 
        -- Log the exact error to the PostgreSQL server logs
        RAISE WARNING 'Disk usage check failed. Error: % (SQLSTATE: %)', SQLERRM, SQLSTATE;
        
        -- Return our error state to the monitoring tool
        RETURN -1;
END;
$$ LANGUAGE plpgsql;
