-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseSCCRepository DROP CONSTRAINT IF EXISTS suse_sccrepo_instup_ck;
ALTER TABLE suseSCCRepository ADD COLUMN IF NOT EXISTS
    installer_updates CHAR(1) DEFAULT ('N') NOT NULL;
ALTER TABLE suseSCCRepository ADD
    CONSTRAINT suse_sccrepo_instup_ck CHECK (installer_updates in ('Y', 'N'));
