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

COMMENT ON TABLE SystemEntitlement
  IS 'Lists the entitlements of a system';

COMMENT ON COLUMN SystemEntitlement.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN SystemEntitlement.system_id
  IS 'The id of the system';
COMMENT ON COLUMN SystemEntitlement.system_group_id
  IS 'The id of this entitlement group';
COMMENT ON COLUMN SystemEntitlement.name
  IS 'The name of this entitlement';
COMMENT ON COLUMN SystemEntitlement.description
  IS 'The description this entitlement';
COMMENT ON COLUMN SystemEntitlement.group_type
  IS 'The id of the type of entitlement';
COMMENT ON COLUMN SystemEntitlement.group_type_name
  IS 'The name of the type of entitlement';
COMMENT ON COLUMN SystemEntitlement.current_members
  IS 'The current number of members of this entitlement';
COMMENT ON COLUMN SystemEntitlement.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN SystemEntitlement.synced_date
  IS 'The timestamp of when this data was last refreshed.';

ALTER TABLE SystemEntitlement
    ADD CONSTRAINT SystemEntitlement_system_fkey FOREIGN KEY (mgm_id, system_id) REFERENCES System(mgm_id, system_id);
