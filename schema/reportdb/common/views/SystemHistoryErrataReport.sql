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

CREATE OR REPLACE VIEW SystemHistoryErrataReport AS
  SELECT mgm_id
            , system_id
            , action_id
            , earliest_action
            , completion_time
            , status
            , event
            , event_data
            , synced_date
    FROM SystemAction
   WHERE event = 'Patch Update'
ORDER BY mgm_id, system_id, action_id
;
