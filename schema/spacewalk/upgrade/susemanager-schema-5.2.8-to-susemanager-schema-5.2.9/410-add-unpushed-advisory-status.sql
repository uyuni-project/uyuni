-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnErrata DROP CONSTRAINT IF EXISTS rhn_errata_adv_status_ck;
ALTER TABLE rhnErrata ADD
    CONSTRAINT rhn_errata_adv_status_ck
    CHECK (advisory_status in ('final', 'stable', 'testing', 'pending',
                               'retracted', 'unpushed'));
