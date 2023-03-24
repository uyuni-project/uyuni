CREATE TABLE rhnActionSubChannels (
    id                  NUMERIC NOT NULL
                            CONSTRAINT rhn_actionsubscrch_id_pk PRIMARY KEY,
    action_id           NUMERIC NOT NULL
                            CONSTRAINT rhn_actionsubscrch_aid_fk
                            REFERENCES rhnAction (id)
                            ON DELETE CASCADE,
    base_channel_id     NUMERIC
                            CONSTRAINT rhn_actionsubscrch_base_ch_fk
                            REFERENCES rhnChannel (id)
                            ON DELETE CASCADE,
    created             TIMESTAMPTZ
                            DEFAULT (CURRENT_TIMESTAMP) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (CURRENT_TIMESTAMP) NOT NULL
)

;

CREATE SEQUENCE RHN_ACT_SUBSCR_CHNLS_ID_SEQ;

CREATE TABLE rhnActionSubChannelsList (
    subscribe_channels_id
                        NUMERIC NOT NULL
                            CONSTRAINT rhn_actionsubscrchls_det_fk
                            REFERENCES rhnActionSubChannels (id)
                            ON DELETE CASCADE,
    channel_id          NUMERIC NOT NULL
                            CONSTRAINT rhn_actionsubscrchls_ch_fk
                            REFERENCES rhnChannel (id)
                            ON DELETE CASCADE
)

;

CREATE TABLE rhnActionSubChannelsTokens (
    subscribe_channels_id
                        NUMERIC NOT NULL
                            CONSTRAINT rhn_actionsubscrchtok_det_fk
                            REFERENCES rhnActionSubChannels (id)
                            ON DELETE CASCADE,
    token_id            NUMERIC NOT NULL
                            CONSTRAINT rhn_actionsubscrchtok_tok_fk
                            REFERENCES suseChannelAccessToken (id)
                            ON DELETE CASCADE
)

;

insert into rhnActionType values (506, 'channels.subscribe', 'Subscribe to channels', 'N', 'N');
