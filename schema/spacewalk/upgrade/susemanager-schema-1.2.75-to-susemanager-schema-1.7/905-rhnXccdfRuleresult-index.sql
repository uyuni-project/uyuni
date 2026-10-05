-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

CREATE INDEX rhn_xccdf_rresult_tresult_idx
    ON rhnXccdfRuleresult (testresult_id)
    TABLESPACE [[4m_tbs]]
    NOLOGGING;
