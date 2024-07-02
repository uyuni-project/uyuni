CREATE TABLE IF NOT EXISTS suseRegTokenChannelAppStream
(
    id          NUMERIC NOT NULL
                  CONSTRAINT suse_reg_tok_ch_as_id_pk PRIMARY KEY,
    token_id    NUMERIC NOT NULL,
    channel_id  NUMERIC NOT NULL,
    name        VARCHAR(128) NOT NULL,
    stream      VARCHAR(128) NOT NULL,

    CONSTRAINT suse_reg_tok_ch_as_id_fk FOREIGN KEY (channel_id, token_id)
        REFERENCES rhnRegTokenChannels (channel_id, token_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS suse_reg_tok_ch_as_name_stream_idx
    ON suseRegTokenChannelAppStream (name, stream);

CREATE SEQUENCE IF NOT EXISTS suse_reg_tok_ch_as_id_seq;
