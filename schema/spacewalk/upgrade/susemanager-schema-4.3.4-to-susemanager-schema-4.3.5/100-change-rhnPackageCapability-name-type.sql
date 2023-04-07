DO $$
    BEGIN
        IF (SELECT data_type FROM information_schema.columns WHERE table_name = 'rhnpackagecapability' AND column_name = 'name') != 'text' THEN
            DROP INDEX IF EXISTS rhn_pkg_cap_name_version_uq;
            DROP INDEX IF EXISTS rhn_pkg_cap_name_uq;

            ALTER TABLE rhnPackageCapability ALTER COLUMN name TYPE text;

            CREATE UNIQUE INDEX rhn_pkg_cap_name_version_uq
                ON rhnPackageCapability USING btree (sha512(replace(name, E'\\', E'\\\\')::bytea), version)
             WHERE version IS NOT NULL;

            CREATE UNIQUE INDEX rhn_pkg_cap_name_uq
                ON rhnPackageCapability USING btree (sha512(replace(name, E'\\', E'\\\\')::bytea))
             WHERE version IS NULL;
        ELSE
          RAISE NOTICE 'rhnPackageCapability column name is already of type text.';
        END IF;
    END;
$$;
