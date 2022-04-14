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

CREATE OR REPLACE VIEW HostGuestsReport AS
  SELECT mgm_id
            , host_system_id AS host
            , virtual_system_id AS guest
            , synced_date
    FROM SystemVirtualData
   WHERE host_system_id IS NOT NULL
            AND virtual_system_id IS NOT NULL
ORDER BY mgm_id, host_system_id, virtual_system_id
;
