-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

CREATE TABLE rhnXccdfRuleIdentMap
(
    rresult_id    NUMBER NOT NULL
                      CONSTRAINT rhn_xccdf_rim_rresult_fk
                          REFERENCES rhnXccdfRuleresult (id)
                          ON DELETE CASCADE,
    ident_id      NUMBER NOT NULL
                      CONSTRAINT rhn_xccdf_rim_ident_fk
                          REFERENCES rhnXccdfIdent (id)
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX rhn_xccdf_rim_ri_uq
    ON rhnXccdfRuleIdentMap (rresult_id, ident_id)
    TABLESPACE [[8m_tbs]]
    NOLOGGING;
