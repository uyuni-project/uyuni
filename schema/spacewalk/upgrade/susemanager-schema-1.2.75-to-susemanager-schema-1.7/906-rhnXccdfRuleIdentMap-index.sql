-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

CREATE INDEX rhn_xccdf_rim_ident_idx
    ON rhnXccdfRuleIdentMap (ident_id)
    TABLESPACE [[8m_tbs]]
    NOLOGGING;
