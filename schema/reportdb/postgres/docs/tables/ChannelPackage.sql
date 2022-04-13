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

COMMENT ON TABLE ChannelPackage
  IS 'The list of packages distributed by a channel';

COMMENT ON COLUMN ChannelPackage.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN ChannelPackage.channel_id
  IS 'The id of the channel';
COMMENT ON COLUMN ChannelPackage.package_id
  IS 'The id of the package';
COMMENT ON COLUMN ChannelPackage.channel_label
  IS 'The label of the channel';
COMMENT ON COLUMN ChannelPackage.package_name
  IS 'The name of the package';
COMMENT ON COLUMN ChannelPackage.package_epoch
  IS 'The package epoch';
COMMENT ON COLUMN ChannelPackage.package_version
  IS 'The package version';
COMMENT ON COLUMN ChannelPackage.package_release
  IS 'The package release number';
COMMENT ON COLUMN ChannelPackage.package_type
  IS 'The type of the package. Possible values: rpm, deb';
COMMENT ON COLUMN ChannelPackage.package_arch
  IS 'The package architecture';
COMMENT ON COLUMN ChannelPackage.synced_date
  IS 'The timestamp of when this data was last refreshed.';

ALTER TABLE ChannelPackage
  ADD CONSTRAINT ChannelPackage_channel_fkey FOREIGN KEY (mgm_id, channel_id) REFERENCES Channel(mgm_id, channel_id),
  ADD CONSTRAINT ChannelPackage_package_fkey FOREIGN KEY (mgm_id, package_id) REFERENCES Package(mgm_id, package_id);
