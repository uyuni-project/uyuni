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

CREATE OR REPLACE VIEW SystemGroupsSystemsReport AS
   SELECT SystemGroupMember.mgm_id
              , SystemGroupMember.system_group_id AS group_id
              , SystemGroupMember.group_name AS group_name
              , System.system_id
              , System.profile_name AS system_name
              , SystemGroupMember.synced_date
     FROM SystemGroupMember
              INNER JOIN System ON ( SystemGroupMember.mgm_id = System.mgm_id AND SystemGroupMember.system_id = System.system_id )
 ORDER BY SystemGroupMember.mgm_id, SystemGroupMember.system_group_id, SystemGroupMember.system_id
;
