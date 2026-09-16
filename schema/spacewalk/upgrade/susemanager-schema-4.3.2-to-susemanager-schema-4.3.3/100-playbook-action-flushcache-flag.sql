-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnActionPlaybook ADD COLUMN IF NOT EXISTS
    flush_cache CHAR(1) DEFAULT ('N') NOT NULL
        CONSTRAINT rhn_action_playbook_flushcache_ck
            CHECK (flush_cache IN ('Y', 'N'));
