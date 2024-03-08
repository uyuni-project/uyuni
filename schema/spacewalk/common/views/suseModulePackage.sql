--
-- Copyright (c) 2024 SUSE LLC
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
CREATE OR REPLACE VIEW suseModulePackage AS
SELECT
    m.name,
    m.stream,
    m.version,
    pn.name as pkg_name,
    pe.evr
FROM
    suseAppstream m
    JOIN suseAppstreamPackage mp ON m.id = mp.module_id
    JOIN rhnPackage p on mp.package_id = p.id
    JOIN rhnPackageName pn ON p.name_id = pn.id
    JOIN rhnPackageEvr pe ON p.evr_id = pe.id
ORDER BY m.name, m.stream, m.version;
