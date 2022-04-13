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

COMMENT ON VIEW SystemHistoryPackagesReport
  IS 'Package event history.';

COMMENT ON COLUMN SystemHistoryPackagesReport.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN SystemHistoryPackagesReport.system_id
  IS 'The id of the system';
COMMENT ON COLUMN SystemHistoryPackagesReport.action_id
  IS 'The id of the action';
COMMENT ON COLUMN SystemHistoryPackagesReport.earliest_action
  IS 'The earliest time this action was schedule for execution';
COMMENT ON COLUMN SystemHistoryPackagesReport.completion_time
  IS 'When this action was completed';
COMMENT ON COLUMN SystemHistoryPackagesReport.status
  IS 'The current status of the action. Possible values Queued, Picked Up, Completed, Failed';
COMMENT ON COLUMN SystemHistoryPackagesReport.event
  IS 'The type of event triggered by this action';
COMMENT ON COLUMN SystemHistoryPackagesReport.event_data
  IS 'Additional information related to the event triggered by this action';
COMMENT ON COLUMN SystemHistoryPackagesReport.synced_date
  IS 'The timestamp of when this data was last refreshed.';
