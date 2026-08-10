--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software; express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

UPDATE access.namespace
SET description = 'Remove ansible path'
WHERE namespace = 'api.ansible.remove_ansible_path'
    AND access_mode = 'W';

UPDATE access.namespace
SET description = 'Update ansible path'
WHERE namespace = 'api.ansible.update_ansible_path'
    AND access_mode = 'W';
