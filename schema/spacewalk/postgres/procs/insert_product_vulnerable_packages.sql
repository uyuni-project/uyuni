--
-- Copyright (c) 2023 SUSE LLC
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