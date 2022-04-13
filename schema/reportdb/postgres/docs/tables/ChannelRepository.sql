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

COMMENT ON TABLE ChannelRepository
  IS 'The list of the repositories of a channel';

COMMENT ON COLUMN ChannelRepository.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN ChannelRepository.channel_id
  IS 'The id of the channel';
COMMENT ON COLUMN ChannelRepository.repository_id
  IS 'The id of the repository';
COMMENT ON COLUMN ChannelRepository.repository_label
  IS 'The unique label of the repository';
COMMENT ON COLUMN ChannelRepository.synced_date
  IS 'The timestamp of when this data was last refreshed.';

ALTER TABLE ChannelRepository
  ADD CONSTRAINT ChannelRepository_channel_fkey FOREIGN KEY (mgm_id, channel_id) REFERENCES Channel(mgm_id, channel_id),
  ADD CONSTRAINT ChannelRepository_repository_fkey FOREIGN KEY (mgm_id, repository_id) REFERENCES Repository(mgm_id, repository_id);
