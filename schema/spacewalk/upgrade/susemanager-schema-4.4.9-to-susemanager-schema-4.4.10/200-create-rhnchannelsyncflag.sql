CREATE TABLE if NOT EXISTS rhnChannelSyncFlag (

    channel_id          NUMERIC NOT NULL
    CONSTRAINT rhn_chsf_cid_pk PRIMARY KEY
        REFERENCES rhnchannel(id),
    
    no_strict   CHAR(1) DEFAULT ('N') NOT NULL
        CONSTRAINT rhn_chsf_no_strict_pk
            CHECK (no_strict IN ('Y', 'N')),

    no_errata   CHAR(1) DEFAULT ('N') NOT NULL
        CONSTRAINT rhn_chsf_no_errata_pk
            CHECK (no_errata IN ('Y', 'N')),

    only_latest CHAR(1) DEFAULT ('N') NOT NULL
        CONSTRAINT rhn_chsf_only_latest_pk
            CHECK (only_latest IN ('Y', 'N')),

    create_tree CHAR(1) DEFAULT ('N') NOT NULL
        CONSTRAINT rhn_chsf_create_tree_pk
            CHECK (create_tree IN ('Y', 'N')),

    quit_on_error CHAR(1) DEFAULT ('N') NOT NULL
        CONSTRAINT rhn_chsf_quit_on_error_pk
            CHECK (quit_on_error IN ('Y', 'N'))
);