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
-- SPDX-License-Identifier: GPL-2.0-only
--

-- Grant again the privileges on the given views which were stored with
-- backup_view_acls(), and remove them from the backup. The backup table is
-- dropped once no privileges are left in it.
--
-- View and schema names are folded to lower case, like unquoted identifiers.
-- The views must exist, otherwise the function fails and the backup is kept.
--
-- Returns the number of restored privileges.
CREATE OR REPLACE FUNCTION restore_view_acls(view_names_in TEXT[], schema_name_in TEXT DEFAULT 'public')
RETURNS INTEGER
AS $$
DECLARE
    acl_entry RECORD;
    restored  INTEGER := 0;
BEGIN
    IF pg_catalog.to_regclass('public.view_acl_backup') IS NULL THEN
        RETURN 0;
    END IF;

    FOR acl_entry IN
        DELETE FROM public.view_acl_backup
         WHERE schema_name = lower(schema_name_in)
           AND view_name IN ( SELECT lower(v) FROM unnest(view_names_in) AS v )
     RETURNING *
    LOOP
        EXECUTE format('GRANT %s ON %I.%I TO %s%s'
                        , acl_entry.privilege_type
                        , acl_entry.schema_name
                        , acl_entry.view_name
                        , COALESCE(quote_ident(acl_entry.grantee), 'PUBLIC')
                        , CASE WHEN acl_entry.is_grantable THEN ' WITH GRANT OPTION' ELSE '' END);
        restored := restored + 1;
    END LOOP;

    IF NOT EXISTS ( SELECT 1 FROM public.view_acl_backup ) THEN
        DROP TABLE public.view_acl_backup;
    END IF;

    RETURN restored;
END;
$$ LANGUAGE plpgsql;
