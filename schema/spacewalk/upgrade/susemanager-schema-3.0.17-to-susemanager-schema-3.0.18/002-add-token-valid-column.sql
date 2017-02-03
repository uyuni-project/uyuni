ALTER TABLE suseChannelAccessToken ADD valid VARCHAR(1) DEFAULT ('N') NOT NULL;

CREATE UNIQUE INDEX suse_channelaccesstoken_token_uq
    ON suseChannelAccessToken (token);
