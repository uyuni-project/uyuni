CREATE TABLE suseChannelAccessToken
(
    id               NUMERIC NOT NULL
                         CONSTRAINT suse_chan_access_token_id_pk PRIMARY KEY,
    minion_id        NUMERIC
                         CONSTRAINT suse_chan_access_token_mid_fk
                             REFERENCES suseMinionInfo (server_id)
                             ON DELETE SET NULL,
    token            VARCHAR(4000) NOT NULL,
    created          TIMESTAMPTZ NOT NULL,
    expiration       TIMESTAMPTZ NOT NULL,
    valid            CHAR(1) DEFAULT ('N') NOT NULL CHECK (valid in ('Y', 'N'))
)

;

CREATE SEQUENCE suse_chan_access_token_id_seq;

CREATE UNIQUE INDEX suse_accesstoken_token_uq
    ON suseChannelAccessToken (token);

CREATE TABLE suseChannelAccessTokenChannel
(
    token_id    NUMERIC NOT NULL
                    CONSTRAINT suse_catc_tid_fk
                        REFERENCES suseChannelAccessToken (id)
                        ON DELETE CASCADE,
    channel_id  NUMERIC NOT NULL
                    CONSTRAINT suse_catc_cid_fk
                        REFERENCES rhnChannel (id)
                        ON DELETE CASCADE,
    created     TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL,
    modified    TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX suse_catc_tid_cid_uq
    ON suseChannelAccessTokenChannel (token_id, channel_id)
    ;

