--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

DELETE FROM access.accessGroupNamespace
WHERE namespace_id IN (
    SELECT id FROM access.namespace
    WHERE namespace LIKE 'software.manage.%'
)
AND group_id IN (
    SELECT id FROM access.accessGroup
    WHERE org_id IS NULL
    AND label <> 'channel_admin'
);
