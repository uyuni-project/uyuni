CREATE TABLE suseChannelAccessToken
(
    id               NUMBER NOT NULL
                         CONSTRAINT suse_chan_access_token_id_pk PRIMARY KEY,
    minion_id        NUMBER
                         CONSTRAINT suse_chan_access_token_mid_fk
                             REFERENCES suseMinionInfo (server_id)
                             ON DELETE SET NULL,
    token            varchar2(4000) NOT NULL,
    created          timestamp with local time zone NOT NULL,
    expiration       timestamp with local time zone NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_chan_access_token_id_seq;

CREATE TABLE suseChannelAccessTokenChannel
(
    token_id    NUMBER NOT NULL
                    CONSTRAINT suse_catc_tid_fk
                        REFERENCES suseChannelAccessToken (id)
                        ON DELETE CASCADE,
    channel_id  NUMBER NOT NULL
                    CONSTRAINT suse_catc_cid_fk
                        REFERENCES rhnChannel (id)
                        ON DELETE CASCADE,
    created     timestamp with local time zone
                    DEFAULT (current_timestamp) NOT NULL,
    modified    timestamp with local time zone
                    DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX suse_catc_tid_cid_uq
    ON suseChannelAccessTokenChannel (token_id, channel_id)
    TABLESPACE [[8m_tbs]];

