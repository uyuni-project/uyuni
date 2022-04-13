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

COMMENT ON TABLE Package
  IS 'The list of the packages managed by a SUSE Manager instance';

COMMENT ON COLUMN Package.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN Package.package_id
  IS 'The id of the package';
COMMENT ON COLUMN Package.name
  IS 'The name of the package';
COMMENT ON COLUMN Package.epoch
  IS 'The epoch of the package';
COMMENT ON COLUMN Package.version
  IS 'The version number of the package';
COMMENT ON COLUMN Package.release
  IS 'The release number of the package';
COMMENT ON COLUMN Package.arch
  IS 'The architecture where this package is installable';
COMMENT ON COLUMN Package.type
  IS 'The type of the package. Possible values: rpm, deb';
COMMENT ON COLUMN Package.package_size
  IS 'The size of the package, in bytes';
COMMENT ON COLUMN Package.payload_size
  IS 'The size of the payload contained in this package, in bytes';
COMMENT ON COLUMN Package.installed_size
  IS 'The final size after the installation of this package, in bytes';
COMMENT ON COLUMN Package.vendor
  IS 'The vendor providing this package';
COMMENT ON COLUMN Package.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN Package.synced_date
  IS 'The timestamp of when this data was last refreshed.';
