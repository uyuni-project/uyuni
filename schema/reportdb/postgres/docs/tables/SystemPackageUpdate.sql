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

COMMENT ON TABLE SystemPackageUpdate
  IS 'The list of packages that can be updated for a system';

COMMENT ON COLUMN SystemPackageUpdate.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN SystemPackageUpdate.system_id
  IS 'The id of the system';
COMMENT ON COLUMN SystemPackageUpdate.package_id
  IS 'The id of the package';
COMMENT ON COLUMN SystemPackageUpdate.name
  IS 'The name of the package';
COMMENT ON COLUMN SystemPackageUpdate.epoch
  IS 'The epoch of the package';
COMMENT ON COLUMN SystemPackageUpdate.version
  IS 'The version number of the package';
COMMENT ON COLUMN SystemPackageUpdate.release
  IS 'The release number of the package';
COMMENT ON COLUMN SystemPackageUpdate.arch
  IS 'The architecture where the package is installed';
COMMENT ON COLUMN SystemPackageUpdate.type
  IS 'The type of the package. Possible values: rpm, deb';
COMMENT ON COLUMN SystemPackageUpdate.is_latest
  IS 'True, if this package is the latest version';
COMMENT ON COLUMN SystemPackageUpdate.synced_date
  IS 'The timestamp of when this data was last refreshed.';

ALTER TABLE SystemPackageUpdate
    ADD CONSTRAINT SystemPackageUpdate_system_fkey FOREIGN KEY (mgm_id, system_id) REFERENCES System(mgm_id, system_id),
    ADD CONSTRAINT SystemPackageUpdate_package_fkey FOREIGN KEY (mgm_id, package_id) REFERENCES Package(mgm_id, package_id);

