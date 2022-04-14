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

CREATE OR REPLACE VIEW ChannelPackagesReport AS
  SELECT Channel.mgm_id
            , Channel.label AS channel_label
            , Channel.name AS channel_name
            , ChannelPackage.package_name
            , ChannelPackage.package_version
            , ChannelPackage.package_release
            , ChannelPackage.package_epoch
            , ChannelPackage.package_arch
            , case when ChannelPackage.package_epoch is not null then ChannelPackage.package_epoch || ':' else '' end || ChannelPackage.package_name || '-' || ChannelPackage.package_version || '-' || ChannelPackage.package_release || '.' || ChannelPackage.package_arch AS full_package_name
            , ChannelPackage.synced_date
    FROM Channel
            INNER JOIN ChannelPackage ON ( Channel.mgm_id = ChannelPackage.mgm_id AND Channel.channel_id = ChannelPackage.channel_id )
ORDER BY Channel.mgm_id, Channel.label, ChannelPackage.package_name, ChannelPackage.package_version, ChannelPackage.package_release, ChannelPackage.package_epoch, ChannelPackage.package_arch
;
