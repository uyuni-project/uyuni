
CREATE INDEX IF NOT EXISTS rhn_action_sub_channels_action_id_idx
on rhnactionsubchannels (action_id);

CREATE INDEX IF NOT EXISTS rhn_action_sub_channels_list_sub_channels_id_idx
on rhnActionSubChannelsList (subscribe_channels_id);

CREATE INDEX IF NOT EXISTS rhn_action_sub_channels_tokens_sub_channels_id_idx
on rhnActionSubChannelsTokens (subscribe_channels_id);