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

CREATE OR REPLACE VIEW SystemGroupsReport AS
  SELECT mgm_id
            , system_group_id
            , name
            , current_members
            , organization
            , synced_date
    FROM SystemGroup
ORDER BY mgm_id, system_group_id
;
