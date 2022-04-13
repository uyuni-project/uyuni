--
-- Copyright (c) 2022 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

COMMENT ON TABLE SystemGroupPermission
  IS 'The list of additional user accounts allowed to access a system group';

COMMENT ON COLUMN SystemGroupPermission.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN SystemGroupPermission.system_group_id
  IS 'The id of this system group';
COMMENT ON COLUMN SystemGroupPermission.account_id
  IS 'The id of the user account';
COMMENT ON COLUMN SystemGroupPermission.group_name
  IS 'The unique name of the system group';
COMMENT ON COLUMN SystemGroupPermission.synced_date
  IS 'The timestamp of when this data was last refreshed.';

ALTER TABLE SystemGroupPermission
    ADD CONSTRAINT SystemGroupPermission_group_fkey FOREIGN KEY (mgm_id, system_group_id) REFERENCES SystemGroup(mgm_id, system_group_id),
    ADD CONSTRAINT SystemGroupPermission_user_fkey FOREIGN KEY (mgm_id, account_id) REFERENCES Account(mgm_id, account_id);
