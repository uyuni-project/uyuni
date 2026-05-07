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

-- Remove access from all standard roles except 'channel_admin'
DELETE FROM access.accessGroupNamespace
WHERE namespace_id IN (
    SELECT id FROM access.namespace WHERE namespace IN (
            'api.packages.remove_package',
            'api.packages.remove_source_package'
    )
) AND group_id IN (
    SELECT id FROM access.accessGroup WHERE label IN (
        'activation_key_admin',
        'image_admin',
        'config_admin',
        'system_group_admin',
        'regular_user'
    )
);
