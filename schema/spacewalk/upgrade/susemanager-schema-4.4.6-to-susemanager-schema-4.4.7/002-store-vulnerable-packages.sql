CREATE TABLE IF NOT EXISTS suseOVALVulnerablePackage
(
    id             NUMERIC NOT NULL
        CONSTRAINT suse_oval_vulnerable_pkg_id_pk PRIMARY KEY,
    name           VARCHAR NOT NULL,
    fix_version    VARCHAR
);

CREATE SEQUENCE IF NOT EXISTS suse_oval_vulnerable_pkg_id_seq START WITH 301;

CREATE UNIQUE INDEX IF NOT EXISTS suse_oval_vulnerable_pkg_name_fix_version ON suseOVALVulnerablePackage(name, fix_version);

CREATE TABLE IF NOT EXISTS suseOVALPlatformVulnerablePackage
(
    platform_id          NUMERIC NOT NULL
        REFERENCES suseOVALPlatform (id),
    cve_id               NUMERIC NOT NULL
        REFERENCES rhnCve (id),
    vulnerable_pkg_id    NUMERIC
        REFERENCES suseOVALVulnerablePackage (id),
    CONSTRAINT suse_oval_platform_vulnerable_pkg_id_pk PRIMARY KEY (platform_id, cve_id, vulnerable_pkg_id)
);

create or replace procedure insert_product_vulnerable_packages(package_name_in varchar, fix_version_in varchar,
                                                               product_cpe_in varchar, cve_name_in varchar)
    language plpgsql
as
$$
declare
    cve_id_val numeric;
    product_cpe_id_val numeric;
    vulnerable_pkg_id_val numeric;
begin

    INSERT INTO rhncve(id, name)
    VALUES (nextval('rhn_cve_id_seq'), cve_name_in)
    ON CONFLICT(name) DO NOTHING;

    SELECT id INTO cve_id_val FROM rhncve WHERE name = cve_name_in;

    INSERT INTO suseovalplatform(id, cpe)
    VALUES (nextval('suse_oval_platform_id_seq'), product_cpe_in)
    ON CONFLICT(cpe) DO NOTHING;

    SELECT id INTO product_cpe_id_val FROM suseOVALPlatform WHERE cpe = product_cpe_in;

    IF not EXISTS(SELECT 1
                  FROM suseovalvulnerablepackage
                  WHERE name = package_name_in
                    AND ((fix_version IS NOT NULL AND fix_version = fix_version_in) OR
                         (fix_version IS NULL AND fix_version_in IS NULL))) THEN
        INSERT INTO suseovalvulnerablepackage(id, name, fix_version)
        VALUES (nextval('suse_oval_vulnerable_pkg_id_seq'), package_name_in, fix_version_in);
    END IF;

    SELECT id
    INTO vulnerable_pkg_id_val
    FROM suseovalvulnerablepackage
    WHERE name = package_name_in
      AND ((fix_version IS NOT NULL AND fix_version = fix_version_in) OR
           (fix_version IS NULL AND fix_version_in IS NULL));

    INSERT INTO suseOVALPlatformVulnerablePackage(platform_id, cve_id, vulnerable_pkg_id)
    VALUES (product_cpe_id_val, cve_id_val, vulnerable_pkg_id_val)
    ON CONFLICT(platform_id, cve_id, vulnerable_pkg_id) DO UPDATE
        SET platform_id       = EXCLUDED.platform_id,
            cve_id            = EXCLUDED.cve_id,
            vulnerable_pkg_id = EXCLUDED.vulnerable_pkg_id;
end;
$$;