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

COMMENT ON VIEW SystemPackagesInstalledReport
  IS 'List of all packages for all systems.';

COMMENT ON COLUMN SystemPackagesInstalledReport.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN SystemPackagesInstalledReport.system_id
  IS 'The id of the system';
COMMENT ON COLUMN SystemPackagesInstalledReport.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN SystemPackagesInstalledReport.package_name
  IS 'The name of the package';
COMMENT ON COLUMN SystemPackagesInstalledReport.package_epoch
  IS 'The epoch of the package';
COMMENT ON COLUMN SystemPackagesInstalledReport.package_version
  IS 'The version number of the package';
COMMENT ON COLUMN SystemPackagesInstalledReport.package_release
  IS 'The release number of the package';
COMMENT ON COLUMN SystemPackagesInstalledReport.package_arch
  IS 'The architecture where the package is installable';
COMMENT ON COLUMN SystemPackagesInstalledReport.synced_date
  IS 'The timestamp of when this data was last refreshed.';
