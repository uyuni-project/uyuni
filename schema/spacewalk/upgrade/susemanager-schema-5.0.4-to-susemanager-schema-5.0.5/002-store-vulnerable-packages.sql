create or replace function
    lookup_oval_platform(platform_cpe_in in varchar)
    returns numeric
as $$
declare
    platform_cpe_in_val     numeric;
begin
    if not exists(select c from suseOVALPlatform c where cpe = platform_cpe_in) then
        insert into suseovalplatform(id, cpe)
        values (nextval('suse_oval_platform_id_seq'), platform_cpe_in);
    end if;

    select id into platform_cpe_in_val FROM suseOVALPlatform WHERE cpe = platform_cpe_in;

    return platform_cpe_in_val;
end;
$$ language plpgsql;

CREATE OR REPLACE PROCEDURE
    insert_product_vulnerable_packages(package_name_in varchar,fix_version_in varchar,product_cpe_in varchar,cve_name_in varchar)
AS
$$
DECLARE
    cve_id_val numeric;
    product_cpe_id_val numeric;
    vulnerable_pkg_id_val numeric;
begin

    cve_id_val := lookup_cve(cve_name_in);

    product_cpe_id_val := lookup_oval_platform(product_cpe_in);

    IF NOT EXISTS(SELECT 1
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
$$ language plpgsql;