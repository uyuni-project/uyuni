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

CREATE OR REPLACE VIEW ChannelsReport AS
  SELECT Channel.mgm_id
            , Channel.channel_id
            , Channel.label AS channel_label
            , Channel.name AS channel_name
            , COUNT(ChannelPackage.channel_id) AS number_of_packages
            , Channel.organization
            , Channel.synced_date
    FROM Channel
            LEFT JOIN ChannelPackage ON ( Channel.mgm_id = ChannelPackage.mgm_id AND Channel.channel_id = ChannelPackage.channel_id )
GROUP BY Channel.mgm_id, Channel.channel_id, Channel.label, Channel.name, Channel.organization, Channel.synced_date
ORDER BY Channel.mgm_id, Channel.channel_id
;
