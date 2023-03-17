
CREATE TABLE rhnXccdfProfile
(
    id            NUMERIC NOT NULL
                      CONSTRAINT rhn_xccdf_profile_id_pk PRIMARY KEY
                      ,
    identifier    VARCHAR(120) NOT NULL,
    title         VARCHAR(120) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_xccdf_profile_it_uq
    ON rhnXccdfProfile (identifier, title)
    
    ;

CREATE SEQUENCE rhn_xccdf_profile_id_seq;
