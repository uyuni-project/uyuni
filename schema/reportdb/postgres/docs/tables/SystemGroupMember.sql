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

COMMENT ON TABLE SystemGroupMember
  IS 'The list of system group a system is member of';

COMMENT ON COLUMN SystemGroupMember.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN SystemGroupMember.system_id
  IS 'The id of the system';
COMMENT ON COLUMN SystemGroupMember.system_group_id
  IS 'The id of the system group';
COMMENT ON COLUMN SystemGroupMember.group_name
  IS 'The unique name of the system group';
COMMENT ON COLUMN SystemGroupMember.system_name
  IS 'The unique descriptive name of the system';
COMMENT ON COLUMN SystemGroupMember.synced_date
  IS 'The timestamp of when this data was last refreshed.';

ALTER TABLE SystemGroupMember
    ADD CONSTRAINT SystemGroupMember_system_fkey FOREIGN KEY (mgm_id, system_id) REFERENCES System(mgm_id, system_id),
    ADD CONSTRAINT SystemGroupMember_group_fkey FOREIGN KEY (mgm_id, system_group_id) REFERENCES SystemGroup(mgm_id, system_group_id);
