
CREATE TABLE rhnXccdfIdent
(
    id              NUMERIC NOT NULL
                        CONSTRAINT rhn_xccdf_ident_id_pk PRIMARY KEY
                        ,
    identsystem_id  NUMERIC NOT NULL
                        CONSTRAINT rhn_xccdf_ident_system_fk
                            REFERENCES rhnXccdfIdentsystem (id),
    identifier      VARCHAR (20) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_xccdf_ident_isi_uq
    ON rhnXccdfIdent (identsystem_id, identifier)
    
    ;

CREATE SEQUENCE rhn_xccdf_ident_id_seq;
