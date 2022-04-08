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

CREATE OR REPLACE VIEW CustomInfoReport AS
  SELECT SystemCustomInfo.mgm_id
            , SystemCustomInfo.system_id
            , System.profile_name AS system_name
            , SystemCustomInfo.organization
            , SystemCustomInfo.key
            , SystemCustomInfo.value
            , SystemCustomInfo.synced_date
    FROM SystemCustomInfo
            INNER JOIN System ON (SystemCustomInfo.mgm_id = System.mgm_id AND SystemCustomInfo.system_id = System.system_id )
ORDER BY SystemCustomInfo.mgm_id, SystemCustomInfo.organization, SystemCustomInfo.system_id, SystemCustomInfo.key
;
