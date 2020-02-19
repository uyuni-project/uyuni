
CREATE TABLE rhnXccdfRuleresultType
(
    id            NUMERIC NOT NULL
                      CONSTRAINT rhn_xccdf_rresult_t_id_pk PRIMARY KEY
                      ,
    abbreviation  CHAR(1) NOT NULL,
    label         VARCHAR(16) NOT NULL,
    description   VARCHAR(120) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_xccdf_rresult_t_label_uq
    ON rhnXccdfRuleresultType (label)
    
    ;
