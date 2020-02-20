
CREATE TABLE rhnActionScap
(
    id               NUMERIC NOT NULL
                         CONSTRAINT rhn_act_scap_id_pk PRIMARY KEY
                         ,
    action_id        NUMERIC NOT NULL
                         CONSTRAINT rhn_act_scap_act_fk
                             REFERENCES rhnAction (id)
                             ON DELETE CASCADE,
    path             VARCHAR(2048) NOT NULL,
    parameters       BYTEA
)


;

CREATE UNIQUE INDEX rhn_act_scap_aid_idx
    ON rhnActionScap (action_id)
    
    ;

CREATE INDEX rhn_act_scap_path_idx
    ON rhnActionScap (path)
    
    ;

CREATE SEQUENCE rhn_act_scap_id_seq;
