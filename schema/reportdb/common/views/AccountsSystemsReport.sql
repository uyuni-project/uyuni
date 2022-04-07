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

CREATE OR REPLACE VIEW AccountsSystemsReport AS
  WITH org_admins AS (
      SELECT mgm_id, account_id
        FROM AccountGroup
       WHERE account_group_type_label = 'org_admin'
  ), system_users AS (
      SELECT true as is_admin
              , Account.mgm_id
              , Account.account_id
              , System.system_id
              , NULL as group_name
        FROM System
              INNER JOIN Account ON ( System.mgm_id = Account.mgm_id AND System.organization = Account.organization )
    UNION
      SELECT false AS is_admin
                , SystemGroupPermission.mgm_id
                , SystemGroupPermission.account_id
                , SystemGroupMember.system_id
                , SystemGroupPermission.group_name
        FROM SystemGroupPermission
                INNER JOIN SystemGroupMember ON ( SystemGroupPermission.mgm_id = SystemGroupMember.mgm_id AND SystemGroupPermission.system_group_id = SystemGroupMember.system_group_id )
  ), users_details AS (
    SELECT Account.mgm_id
              , Account.account_id
              , Account.username
              , Account.organization
              , org_admins.account_id IS NOT NULL AS is_admin
              , Account.synced_date
      FROM Account
              LEFT JOIN org_admins ON ( Account.mgm_id = org_admins.mgm_id AND Account.account_id = org_admins.account_id )
  )
  SELECT users_details.mgm_id
            , users_details.account_id
            , users_details.username
            , users_details.organization
            , system_users.system_id
            , system_users.group_name
            , users_details.is_admin
            , users_details.synced_date
    FROM users_details
            LEFT JOIN system_users ON ( users_details.mgm_id = system_users.mgm_id AND users_details.is_admin = system_users.is_admin AND users_details.account_id = system_users.account_id)
   WHERE system_users.system_id IS NOT NULL
ORDER BY users_details.mgm_id, users_details.account_id, system_users.system_id
;
