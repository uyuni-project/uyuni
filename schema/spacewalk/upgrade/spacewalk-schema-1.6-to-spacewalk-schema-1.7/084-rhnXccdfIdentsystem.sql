
CREATE TABLE rhnXccdfIdentsystem
(
    id      NUMERIC NOT NULL
                CONSTRAINT rhn_xccdf_identsytem_id_pk PRIMARY KEY
                ,
    system  VARCHAR(80) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_xccdf_identsystem_id_uq
    ON rhnXccdfIdentsystem (system)
    
    ;

CREATE SEQUENCE rhn_xccdf_identsytem_id_seq;
