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

CREATE OR REPLACE VIEW AccountsReport AS
  SELECT Account.mgm_id
            , Account.organization
            , Account.account_id
            , Account.username
            , Account.last_name
            , Account.first_name
            , Account.position
            , Account.email
            , string_agg(AccountGroup.account_group_type_name, ';') AS roles
            , Account.creation_time
            , Account.last_login_time
            , Account.status
            , Account.md5_encryption
            , Account.synced_date
    FROM Account
            LEFT JOIN AccountGroup ON ( Account.mgm_id = AccountGroup.mgm_id AND Account.account_id = AccountGroup.account_id )
GROUP BY Account.mgm_id
            , Account.organization
            , Account.account_id
            , Account.username
            , Account.last_name
            , Account.first_name
            , Account.position
            , Account.email
            , Account.creation_time
            , Account.last_login_time
            , Account.status
            , Account.md5_encryption
            , Account.synced_date
ORDER BY Account.mgm_id, Account.organization, Account.account_id
;
