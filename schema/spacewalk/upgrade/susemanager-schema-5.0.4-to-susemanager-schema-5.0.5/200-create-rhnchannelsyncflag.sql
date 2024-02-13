CREATE TABLE if NOT EXISTS rhnChannelSyncFlag (
    channel_id          NUMERIC NOT NULL
    CONSTRAINT rhn_chsf_cid_pk REFERENCES rhnchannel(id)
    ON DELETE CASCADE,

    no_strict   BOOLEAN NOT NULL DEFAULT FALSE,
    no_errata   BOOLEAN NOT NULL DEFAULT FALSE,
    only_latest BOOLEAN NOT NULL DEFAULT FALSE,
    create_tree BOOLEAN NOT NULL DEFAULT FALSE,
    quit_on_error BOOLEAN NOT NULL DEFAULT FALSE
);
