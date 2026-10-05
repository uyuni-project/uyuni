-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO rhnXccdfRuleIdentMap
    (rresult_id, ident_id)
    SELECT xrr.id, xrr.ident_id
        FROM rhnXccdfRuleresult xrr;

COMMIT;
