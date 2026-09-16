-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnActionPlaybook ADD COLUMN IF NOT EXISTS
    test_mode CHAR(1) DEFAULT ('N') NOT NULL
        CONSTRAINT rhn_action_playbook_testmode_ck
            CHECK (test_mode IN ('Y', 'N'));
