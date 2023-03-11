-- oracle equivalent source none
CREATE TABLE IF NOT EXISTS rhnActionVirtDelete
(
    action_id  NUMERIC NOT NULL
                   CONSTRAINT rhn_avdl_aid_fk
                       REFERENCES rhnAction (id)
                       ON DELETE CASCADE
                    CONSTRAINT rhn_avdl_aid_pk PRIMARY KEY,
    uuid       VARCHAR(128) NOT NULL,
    created    TIMESTAMPTZ
               DEFAULT (current_timestamp) NOT NULL,
    modified   TIMESTAMPTZ
               DEFAULT (current_timestamp) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS rhn_avdl_aid_uq
    ON rhnActionVirtDelete (action_id);
