-- Remember the privileges granted on the given views, so that they can be
-- replayed with restore_view_acls() after the views were dropped and
-- re-created in a schema migration (dropping a view removes its privileges).
--
-- View and schema names are folded to lower case, like unquoted identifiers.
--
-- The privileges are stored in the table view_acl_backup, which is created on
-- demand and dropped again by restore_view_acls() once it is empty. As it is a
-- regular table, the backup survives a failed migration and can be used across
-- migration files. Calling this function again merges the current privileges
-- into the existing backup, so it is safe in migrations applied more than once.
--
-- Returns the number of stored privileges.
CREATE OR REPLACE FUNCTION backup_view_acls(view_names_in TEXT[], schema_name_in TEXT DEFAULT 'public')
RETURNS INTEGER
AS $$
DECLARE
    stored INTEGER;
BEGIN
    -- keep this lower case, check_reportdb_doc expects documentation for
    -- every line starting with "CREATE TABLE" in main.sql
    create table if not exists public.view_acl_backup (
        schema_name     TEXT NOT NULL,
        view_name       TEXT NOT NULL,
        -- NULL means the privilege was granted to PUBLIC
        grantee         TEXT,
        privilege_type  TEXT NOT NULL,
        is_grantable    BOOLEAN NOT NULL
    );

    CREATE UNIQUE INDEX IF NOT EXISTS view_acl_backup_uq
        ON public.view_acl_backup (schema_name, view_name, COALESCE(grantee, ''), privilege_type);

    INSERT INTO public.view_acl_backup (schema_name, view_name, grantee, privilege_type, is_grantable)
      SELECT n.nspname
                , c.relname
                , CASE WHEN acl.grantee = 0 THEN NULL ELSE pg_catalog.pg_get_userbyid(acl.grantee) END
                , acl.privilege_type
                , acl.is_grantable
        FROM pg_catalog.pg_class c
                INNER JOIN pg_catalog.pg_namespace n ON ( n.oid = c.relnamespace )
                CROSS JOIN LATERAL pg_catalog.aclexplode(c.relacl) AS acl
       WHERE n.nspname = lower(schema_name_in)
         AND c.relkind IN ( 'v', 'm' )
         AND c.relname IN ( SELECT lower(v) FROM unnest(view_names_in) AS v )
         -- the privileges of the owner are granted again when the view is re-created
         AND acl.grantee <> c.relowner
    -- privileges stored by an earlier, failed run are kept, e.g. when the view
    -- was already dropped or re-created without its privileges
    ON CONFLICT (schema_name, view_name, COALESCE(grantee, ''), privilege_type)
    DO UPDATE SET is_grantable = EXCLUDED.is_grantable;

    GET DIAGNOSTICS stored = ROW_COUNT;
    RETURN stored;
END;
$$ LANGUAGE plpgsql;

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
