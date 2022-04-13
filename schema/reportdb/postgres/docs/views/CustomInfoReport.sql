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

COMMENT ON VIEW CustomInfoReport
  IS 'Display all custom info for every system with any info associated';

COMMENT ON COLUMN CustomInfoReport.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN CustomInfoReport.system_id
  IS 'The id of the system';
COMMENT ON COLUMN CustomInfoReport.system_name
  IS 'The unique descriptive name of the system';
COMMENT ON COLUMN CustomInfoReport.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN CustomInfoReport.key
  IS 'The name of the custom information';
COMMENT ON COLUMN CustomInfoReport.value
  IS 'The value of the custom information';
COMMENT ON COLUMN CustomInfoReport.synced_date
  IS 'The timestamp of when this data was last refreshed.';
