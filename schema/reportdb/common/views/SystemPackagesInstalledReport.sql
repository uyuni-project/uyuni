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

CREATE OR REPLACE VIEW SystemPackagesInstalledReport AS
  SELECT SystemPackageInstalled.mgm_id
            , SystemPackageInstalled.system_id
            , System.organization
            , SystemPackageInstalled.name AS package_name
            , SystemPackageInstalled.epoch AS package_epoch
            , SystemPackageInstalled.version AS package_version
            , SystemPackageInstalled.release AS package_release
            , SystemPackageInstalled.arch AS package_arch
            , SystemPackageInstalled.synced_date
    FROM SystemPackageInstalled
             INNER JOIN System ON System.system_id = SystemPackageInstalled.system_id
ORDER BY SystemPackageInstalled.mgm_id, SystemPackageInstalled.system_id, SystemPackageInstalled.name
;
