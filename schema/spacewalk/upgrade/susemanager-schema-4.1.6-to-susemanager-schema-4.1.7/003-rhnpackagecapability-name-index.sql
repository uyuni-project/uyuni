CREATE INDEX IF NOT EXISTS rhn_pkg_cap_name_idx
    ON rhnPackageCapability USING HASH (name);
