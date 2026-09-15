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
-- SPDX-License-Identifier: GPL-2.0-only
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--
CREATE OR REPLACE VIEW suseServerAppStreamPackageView AS
SELECT
    p.package_id,
    c.id AS channel_id,
    c.label AS channel_label,
    s.server_id
FROM suseAppstream m
JOIN suseAppstreamPackage p ON p.module_id = m.id
JOIN rhnchannel c ON m.channel_id = c.id
JOIN rhnServerChannel sc ON c.id = sc.channel_id
JOIN suseServerAppstream s
    ON  m.name = s.name
    AND s.server_id = sc.server_id
    AND m.stream = s.stream
    AND m.arch = s.arch;
