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

CREATE OR REPLACE VIEW ActionsReport AS
  SELECT DISTINCT SystemAction.action_id
             , SystemAction.earliest_action
             , SystemAction.event
             , SystemAction.action_name
             , SystemAction.scheduled_by
             , string_agg(SystemAction.hostname, ';') FILTER(WHERE status = 'Picked Up' OR status = 'Queued') OVER(PARTITION BY action_id) AS in_progress_systems
             , string_agg(SystemAction.hostname, ';') FILTER(WHERE status = 'Completed') OVER(PARTITION BY action_id) AS completed_systems
             , string_agg(SystemAction.hostname, ';') FILTER(WHERE status = 'Failed') OVER(PARTITION BY action_id) AS failed_systems
             , SystemAction.archived
    FROM SystemAction
;
