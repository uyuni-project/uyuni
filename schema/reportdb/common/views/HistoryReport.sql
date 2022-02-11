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

CREATE OR REPLACE VIEW HistoryReport AS
      SELECT mgm_id
                , system_id
                , action_id AS event_id
                , hostname
                , event
                , completion_time AS event_time
                , status
                , event_data
                , synced_date
        FROM SystemAction

    UNION ALL

      SELECT mgm_id
                , system_id
                , history_id AS event_id
                , hostname
                , event
                , event_time
                , 'Done' AS status
                , event_data
                , synced_date
        FROM SystemHistory
;
