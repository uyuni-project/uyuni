ALTER TABLE suseOVALPlatformVulnerable ADD COLUMN IF NOT EXISTS last_modified TIMESTAMPTZ DEFAULT current_timestamp NOT NULL;
ALTER TABLE suseOVALVulnerablePackage ADD COLUMN IF NOT EXISTS last_modified TIMESTAMPTZ DEFAULT current_timestamp NOT NULL;

CREATE OR REPLACE PROCEDURE
insert_product_vulnerable_packages(package_name_in varchar, fix_epoch_in varchar, fix_version_in varchar, fix_release_in varchar, fix_type_in varchar, product_cpe_in varchar, product_os_family_in varchar, product_os_version_in varchar, cve_name_in varchar)
AS
$$
DECLARE
    cve_id_val numeric;
    product_cpe_id_val numeric;
    platform_vulnerable_id_val bigint;
    fix_version_evrt evr_t;
    product_os_id_val numeric;
begin

    cve_id_val := lookup_cve(cve_name_in);

    product_cpe_id_val := lookup_oval_platform(product_cpe_in);

    -- Check if the suseOVALOsProduct exists, if not insert it.
    IF NOT EXISTS (
        SELECT 1
        FROM suseOVALOsProduct
        WHERE os_family = product_os_family_in
          AND version = product_os_version_in
    ) THEN
        INSERT INTO suseOVALOsProduct(id, os_family, version)
        VALUES (nextval('suse_oval_os_product_id_seq'), product_os_family_in, product_os_version_in);
    END IF;

    SELECT id INTO product_os_id_val
    FROM suseOVALOsProduct
    WHERE os_family = product_os_family_in AND version = product_os_version_in;

    IF fix_version_in IS NULL THEN
      fix_version_evrt := NULL;
    ELSE
      fix_version_evrt := evr_t(fix_epoch_in, fix_version_in, fix_release_in, fix_type_in);
    END IF;

    INSERT INTO suseOVALPlatformVulnerable(platform_id, product_os_id, cve_id, last_modified)
    VALUES (product_cpe_id_val, product_os_id_val, cve_id_val, current_timestamp)
    ON CONFLICT (platform_id, product_os_id, cve_id) DO
    UPDATE SET last_modified = current_timestamp
    RETURNING id INTO platform_vulnerable_id_val;

    INSERT INTO suseOVALVulnerablePackage(plat_vuln_id, name, fix_version, last_modified)
    VALUES (platform_vulnerable_id_val, package_name_in, fix_version_evrt, current_timestamp)
    ON CONFLICT(plat_vuln_id, name) DO
      UPDATE SET fix_version = EXCLUDED.fix_version, last_modified = current_timestamp;
end;
$$ language plpgsql;
