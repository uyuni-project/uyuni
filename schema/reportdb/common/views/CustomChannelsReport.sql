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

CREATE OR REPLACE VIEW CustomChannelsReport AS
  WITH repositories AS (
      SELECT mgm_id, channel_id, string_agg(repository_id || ' - ' || repository_label, ';')  AS channel_repositories
        FROM ChannelRepository
    GROUP BY mgm_id, channel_id
  )
  SELECT Channel.mgm_id
             , Channel.organization
             , Channel.channel_id
             , Channel.label
             , Channel.name
             , Channel.summary
             , Channel.description
             , Channel.parent_channel_label
             , Channel.arch
             , Channel.checksum_type
             , repositories.channel_repositories
             , Channel.synced_date
    FROM Channel
             LEFT JOIN repositories ON ( Channel.mgm_id = repositories.mgm_id AND Channel.channel_id = repositories.channel_id )
   WHERE Channel.organization IS NOT NULL
ORDER BY Channel.mgm_id, Channel.organization, Channel.channel_id
;
