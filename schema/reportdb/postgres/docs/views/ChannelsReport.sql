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

COMMENT ON VIEW ChannelsReport
  IS 'List of all channels with number of packages in each channel.';

COMMENT ON COLUMN ChannelsReport.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN ChannelsReport.channel_id
  IS 'The id of the channel';
COMMENT ON COLUMN ChannelsReport.channel_label
  IS 'The unique label identifying this channel';
COMMENT ON COLUMN ChannelsReport.channel_name
  IS 'The unique name of the channel';
COMMENT ON COLUMN ChannelsReport.number_of_packages
  IS 'The number of packages provided by the channel';
COMMENT ON COLUMN ChannelsReport.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN ChannelsReport.synced_date
  IS 'The timestamp of when this data was last refreshed.';
