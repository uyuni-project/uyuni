DROP PROCEDURE IF EXISTS insert_product_vulnerable_packages(character varying, character varying, character varying, character varying);

CREATE OR REPLACE PROCEDURE
insert_product_vulnerable_packages(package_name_in varchar, fix_epoch_in varchar, fix_version_in varchar, fix_release_in varchar, fix_type_in varchar, product_cpe_in varchar, cve_name_in varchar)
AS
$$
DECLARE
    cve_id_val numeric;
    product_cpe_id_val numeric;
    platform_vulnerable_id_val bigint;
    fix_version_evrt evr_t;
begin

    cve_id_val := lookup_cve(cve_name_in);

    product_cpe_id_val := lookup_oval_platform(product_cpe_in);

    IF fix_version_in IS NULL THEN
      fix_version_evrt := NULL;
    ELSE
      fix_version_evrt := evr_t(fix_epoch_in, fix_version_in, fix_release_in, fix_type_in);
    END IF;

    INSERT INTO suseOVALPlatformVulnerable(platform_id, cve_id)
    VALUES (product_cpe_id_val, cve_id_val)
    ON CONFLICT (platform_id, cve_id) DO
    UPDATE SET platform_id = suseOVALPlatformVulnerable.platform_id
    RETURNING id INTO platform_vulnerable_id_val;

    INSERT INTO suseOVALVulnerablePackage(plat_vuln_id, name, fix_version)
    VALUES (platform_vulnerable_id_val, package_name_in, fix_version_evrt)
    ON CONFLICT(plat_vuln_id, name) DO
      UPDATE SET fix_version = EXCLUDED.fix_version;
end;
$$ language plpgsql;

DROP TABLE IF EXISTS suseOVALPlatformVulnerablePackage;
DROP TABLE IF EXISTS suseOvalVulnerablePackage;
DROP TABLE IF EXISTS suseOVALPlatformVulnerable;

DROP SEQUENCE IF EXISTS suse_oval_vulnerable_pkg_id_seq;

CREATE TABLE suseOVALPlatformVulnerable
(
    id                   BIGINT
                            GENERATED ALWAYS AS IDENTITY
                            CONSTRAINT suse_oval_plat_vuln_id_pk PRIMARY KEY,
    platform_id          NUMERIC NOT NULL
                            REFERENCES suseOVALPlatform (id)
                            ON DELETE CASCADE,
    cve_id               NUMERIC NOT NULL
                            REFERENCES rhnCve (id),
                         CONSTRAINT platform_cve_id_uq UNIQUE (platform_id, cve_id)
);

CREATE INDEX IF NOT EXISTS suse_oval_plat_vuln_plat_id_idx
ON suseOVALPlatformVulnerable(platform_id);
CREATE INDEX IF NOT EXISTS suse_oval_plat_vuln_cve_id_idx
ON suseOVALPlatformVulnerable(cve_id);
CREATE INDEX IF NOT EXISTS suse_oval_plat_vuln_plat_cve_id_idx
ON suseOVALPlatformVulnerable(platform_id, cve_id);

CREATE TABLE suseOVALVulnerablePackage
(
    id             BIGINT
                      GENERATED ALWAYS AS IDENTITY
                      CONSTRAINT suse_oval_vulnerable_pkg_id_pk PRIMARY KEY,
    plat_vuln_id   BIGINT NOT NULL
                      REFERENCES suseOVALPlatformVulnerable(id)
                      ON DELETE CASCADE,
    name           VARCHAR NOT NULL,
    fix_version    evr_t,
                   CONSTRAINT plat_vuln_id_name_uq UNIQUE (plat_vuln_id, name)
);

CREATE INDEX IF NOT EXISTS suse_oval_vulnerable_pkg_plat_vuln_id_idx
ON suseOVALVulnerablePackage(plat_vuln_id);
CREATE INDEX IF NOT EXISTS suse_oval_vulnerable_pkg_name_idx
ON suseOVALVulnerablePackage(name);
CREATE INDEX IF NOT EXISTS suse_oval_vulnerable_pkg_plat_vuln_id_name_idx
ON suseOVALVulnerablePackage(plat_vuln_id, name);
