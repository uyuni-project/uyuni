
CREATE TABLE rhnXccdfRuleresult
(
    testresult_id NUMERIC NOT NULL
                      CONSTRAINT rhn_xccdf_rresult_tresult_fk
                          REFERENCES rhnXccdfTestresult (id)
                          ON DELETE CASCADE,
    ident_id      NUMERIC NOT NULL
                      CONSTRAINT rhn_xccdf_rresult_ident_fk
                          REFERENCES rhnXccdfIdent (id),
    result_id     NUMERIC NOT NULL
                      CONSTRAINT rhn_xccdf_rresult_result_fk
                          REFERENCES rhnXccdfRuleresultType (id)
)

;

CREATE UNIQUE INDEX rhn_xccdf_rresult_tri_uq
    ON rhnXccdfRuleresult (testresult_id, ident_id)
    
    ;
