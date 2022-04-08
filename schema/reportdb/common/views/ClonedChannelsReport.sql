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

CREATE OR REPLACE VIEW ClonedChannelsReport AS
  SELECT original.mgm_id
            , original.channel_id AS original_channel_id
            , original.label AS original_channel_label
            , original.name AS original_channel_name
            , cloned.channel_id AS new_channel_id
            , cloned.label AS new_channel_label
            , cloned.name AS new_channel_name
            , cloned.synced_date
    FROM Channel original
            INNER JOIN Channel cloned ON ( cloned.mgm_id = original.mgm_id AND cloned.original_channel_id = original.channel_id )
ORDER BY original.mgm_id, original.channel_id
;
