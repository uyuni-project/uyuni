DROP INDEX IF EXISTS rhn_pkg_cap_name_version_uq;
DROP INDEX IF EXISTS rhn_pkg_cap_name_uq;

ALTER TABLE rhnPackageCapability ALTER COLUMN name TYPE text;

CREATE UNIQUE INDEX rhn_pkg_cap_name_version_uq
    ON rhnPackageCapability USING btree (sha512(replace(name, E'\\', E'\\\\')::bytea), version)
 WHERE version IS NOT NULL;

CREATE UNIQUE INDEX rhn_pkg_cap_name_uq
    ON rhnPackageCapability USING btree (sha512(replace(name, E'\\', E'\\\\')::bytea))
 WHERE version IS NULL;
