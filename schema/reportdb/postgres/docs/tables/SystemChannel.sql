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

COMMENT ON TABLE SystemChannel
  IS 'The list of channels a system is subscribed to';

COMMENT ON COLUMN SystemChannel.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN SystemChannel.system_id
  IS 'The id of the system';
COMMENT ON COLUMN SystemChannel.channel_id
  IS 'The id of the channel';
COMMENT ON COLUMN SystemChannel.name
  IS 'The name of the channel';
COMMENT ON COLUMN SystemChannel.description
  IS 'A detailed description of the channel scope and purpose';
COMMENT ON COLUMN SystemChannel.architecture_name
  IS 'The architecture of the packages hold by this channel';
COMMENT ON COLUMN SystemChannel.parent_channel_id
  IS 'The id of the parent of this channel, if exists';
COMMENT ON COLUMN SystemChannel.parent_channel_name
  IS 'The name of the parent of this channel, if exists';
COMMENT ON COLUMN SystemChannel.synced_date
  IS 'The timestamp of when this data was last refreshed.';

ALTER TABLE SystemChannel
    ADD CONSTRAINT SystemChannel_system_fkey FOREIGN KEY (mgm_id, system_id) REFERENCES System(mgm_id, system_id),
    ADD CONSTRAINT SystemChannel_channel_fkey FOREIGN KEY (mgm_id, channel_id) REFERENCES Channel(mgm_id, channel_id);
