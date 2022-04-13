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

COMMENT ON VIEW SystemInactivityReport
  IS 'List of the inactivity period of all systems.';

COMMENT ON COLUMN SystemInactivityReport.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN SystemInactivityReport.system_id
  IS 'The id of the system';
COMMENT ON COLUMN SystemInactivityReport.system_name
  IS 'The unique descriptive name of the system';
COMMENT ON COLUMN SystemInactivityReport.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN SystemInactivityReport.last_checkin_time
  IS 'When this system was visible and reachable last time';
COMMENT ON COLUMN SystemInactivityReport.inactivity
  IS 'The period of inactivity';
COMMENT ON COLUMN SystemInactivityReport.synced_date
  IS 'The timestamp of when this data was last refreshed.';
