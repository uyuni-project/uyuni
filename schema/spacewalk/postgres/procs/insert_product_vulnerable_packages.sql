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
