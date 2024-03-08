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
CREATE OR REPLACE VIEW suseServerModularPackages AS
SELECT
    c.id AS channel_id,
    c.label AS channel_label,
    m.id AS module_id,
    m.name,
    m.stream,
    m.version,
    m.context,
    m.arch,
    CONCAT(rpn.name, '-', COALESCE(rpe.epoch || ':', rpe.epoch), rpe.version, '-', rpe.release) AS package,
    s.server_id
FROM suseAppstream m
JOIN suseAppstreamPackage p ON p.module_id = m.id
JOIN rhnchannel c ON m.channel_id = c.id
JOIN rhnpackage rp ON rp.id = p.package_id
JOIN rhnpackageevr rpe ON rpe.id = rp.evr_id
JOIN rhnpackagename rpn ON rpn.id = rp.name_id
LEFT JOIN suseServerAppstream s
    ON  m.name = s.name
    AND m.stream = s.stream
    AND m.version = s.version
    AND m.context = s.context
    AND m.arch = s.arch
ORDER BY c.label, m.name, m.stream, m.version, m.context, m.arch, rpn.name;
