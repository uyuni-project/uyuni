-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnActionChain DROP CONSTRAINT IF EXISTS rhn_actionchain_dispatched_ck;
ALTER TABLE rhnActionChain ADD COLUMN IF NOT EXISTS dispatched CHAR(1) DEFAULT ('N') NOT NULL;
ALTER TABLE rhnActionChain ADD CONSTRAINT rhn_actionchain_dispatched_ck CHECK (dispatched in ('Y', 'N'));
