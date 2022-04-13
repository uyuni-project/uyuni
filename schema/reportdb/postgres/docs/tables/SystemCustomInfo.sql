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

COMMENT ON TABLE SystemCustomInfo
  IS 'List custom pieces of information related to a system';

COMMENT ON COLUMN SystemCustomInfo.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN SystemCustomInfo.system_id
  IS 'The id of the system';
COMMENT ON COLUMN SystemCustomInfo.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN SystemCustomInfo.key
  IS 'The name of the custom information';
COMMENT ON COLUMN SystemCustomInfo.description
  IS 'A brief description of this information';
COMMENT ON COLUMN SystemCustomInfo.value
  IS 'The actual value of the custom information';
COMMENT ON COLUMN SystemCustomInfo.synced_date
  IS 'The timestamp of when this data was last refreshed.';

ALTER TABLE SystemCustomInfo
    ADD CONSTRAINT SystemCustomInfo FOREIGN KEY (mgm_id, system_id) REFERENCES System(mgm_id, system_id);
