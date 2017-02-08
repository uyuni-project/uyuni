ALTER TABLE suseChannelAccessToken ADD valid CHAR(1) DEFAULT ('N') NOT NULL;

UPDATE suseChannelAccessToken
    SET valid='Y'
    WHERE minion_id IS NOT NULL;

CREATE UNIQUE INDEX suse_channelaccesstoken_token_uq
    ON suseChannelAccessToken (token);
