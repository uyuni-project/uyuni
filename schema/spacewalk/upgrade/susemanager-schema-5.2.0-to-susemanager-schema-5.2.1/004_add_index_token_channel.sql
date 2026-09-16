-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

CREATE INDEX IF NOT EXISTS rhn_action_sub_channels_action_id_idx
on rhnactionsubchannels (action_id);

CREATE INDEX IF NOT EXISTS rhn_action_sub_channels_list_sub_channels_id_idx
on rhnActionSubChannelsList (subscribe_channels_id);

CREATE INDEX IF NOT EXISTS rhn_action_sub_channels_tokens_sub_channels_id_idx
on rhnActionSubChannelsTokens (subscribe_channels_id);

CREATE INDEX IF NOT EXISTS rhn_cnp_cid_n_id_idx
    ON rhnChannelNewestPackage(channel_id, name_id);