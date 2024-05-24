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
CREATE OR REPLACE VIEW suseServerAppStreamHiddenPackagesView AS

-- If a package is part of any appstream,
-- and this appstream is not enabled in
-- a server, it should appear here.
SELECT DISTINCT sasp.package_id AS pid, sc.server_id AS sid
FROM rhnserverchannel sc
    INNER JOIN suseappstream sas ON sas.channel_id = sc.channel_id
    INNER JOIN suseappstreampackage sasp ON sasp.module_id = sas.id
    LEFT JOIN suseserverappstream ssa ON ssa.name = sas.name
        AND ssa.stream = sas.stream
        AND sas.arch = ssa.arch
WHERE ssa.id IS NULL

UNION

-- If a package is part of an enabled appstream, all the packages
-- whose name matches with appstream api need to be filtered out
-- except the packages that are part of the enabled appstream.
SELECT DISTINCT p.id AS pid, server_stream.server_id AS sid
FROM suseServerAppstream server_stream
    INNER JOIN suseAppstream appstream ON appstream.name = server_stream.name
        AND appstream.stream = server_stream.stream
        AND appstream.arch = server_stream.arch
    INNER JOIN suseAppstreamApi api ON api.module_id = appstream.id
    inner join rhnPackageName pn ON pn.name = api.rpm
    inner join rhnPackage p ON p.name_id = pn.id
WHERE NOT EXISTS (
    SELECT package_id
    FROM suseServerAppStreamPackageView
    WHERE server_id = server_stream.server_id
        AND package_id = p.id
);
