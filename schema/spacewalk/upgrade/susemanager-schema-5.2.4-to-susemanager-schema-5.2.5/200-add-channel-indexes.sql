CREATE INDEX IF NOT EXISTS rhn_action_sub_channels_tokens_token_id_idx
    ON rhnActionSubChannelsTokens (token_id);

CREATE INDEX IF NOT EXISTS suse_accesstoken_minion_id_idx
    ON suseChannelAccessToken (minion_id);

CREATE INDEX IF NOT EXISTS suse_catc_cid_idx
    ON suseChannelAccessTokenChannel (channel_id);
