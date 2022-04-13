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

COMMENT ON VIEW AccountsSystemsReport
  IS 'Systems administered by individual users';

COMMENT ON COLUMN AccountsSystemsReport.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN AccountsSystemsReport.account_id
  IS 'The id of the user account';
COMMENT ON COLUMN AccountsSystemsReport.username
  IS 'The username used to login';
COMMENT ON COLUMN AccountsSystemsReport.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN AccountsSystemsReport.system_id
  IS 'The id of the system';
COMMENT ON COLUMN AccountsSystemsReport.group_name
  IS 'The name of the group the user belongs to that grants access to the system';
COMMENT ON COLUMN AccountsSystemsReport.is_admin
  IS 'true, if the user has administrative role';
COMMENT ON COLUMN AccountsSystemsReport.synced_date
  IS 'The timestamp of when this data was last refreshed.';
