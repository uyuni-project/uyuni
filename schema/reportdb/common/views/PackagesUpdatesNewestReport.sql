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

CREATE OR REPLACE VIEW PackagesUpdatesNewestReport AS
  SELECT System.mgm_id
            , System.system_id
            , System.organization
            , SystemPackageUpdate.name AS package_name
            , SystemPackageInstalled.epoch AS package_epoch
            , SystemPackageInstalled.version AS package_version
            , SystemPackageInstalled.release AS package_release
            , SystemPackageInstalled.arch AS package_arch
            , SystemPackageUpdate.epoch AS newer_epoch
            , SystemPackageUpdate.version AS newer_version
            , SystemPackageUpdate.release AS newer_release
            , SystemPackageUpdate.synced_date
    FROM System
            INNER JOIN SystemPackageUpdate ON ( System.mgm_id = SystemPackageUpdate.mgm_id AND System.system_id = SystemPackageUpdate.system_id )
            INNER JOIN SystemPackageInstalled ON ( System.mgm_id = SystemPackageInstalled.mgm_id AND System.system_id = SystemPackageInstalled.system_id AND SystemPackageInstalled.name = SystemPackageUpdate.name )
   WHERE SystemPackageUpdate.is_latest
ORDER BY System.mgm_id, System.organization, System.system_id, SystemPackageUpdate.name
;
