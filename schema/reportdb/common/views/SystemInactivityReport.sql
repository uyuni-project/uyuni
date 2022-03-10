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

CREATE OR REPLACE VIEW SystemInactivityReport AS
  SELECT mgm_id
            , system_id
            , profile_name AS system_name
            , organization
            , last_checkin_time
            , (current_timestamp - last_checkin_time) AS inactivity
            , synced_date
    FROM system
ORDER BY mgm_id, system_id, organization
;
