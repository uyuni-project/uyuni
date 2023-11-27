
CREATE TABLE rhnXccdfRuleIdentMap
(
    rresult_id    NUMERIC NOT NULL
                      CONSTRAINT rhn_xccdf_rim_rresult_fk
                          REFERENCES rhnXccdfRuleresult (id)
                          ON DELETE CASCADE,
    ident_id      NUMERIC NOT NULL
                      CONSTRAINT rhn_xccdf_rim_ident_fk
                          REFERENCES rhnXccdfIdent (id)
)

;

CREATE UNIQUE INDEX rhn_xccdf_rim_ri_uq
    ON rhnXccdfRuleIdentMap (rresult_id, ident_id)
    
    ;
