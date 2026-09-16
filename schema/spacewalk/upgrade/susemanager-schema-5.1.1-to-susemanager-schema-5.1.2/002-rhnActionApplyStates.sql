-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnActionApplyStates ADD COLUMN IF NOT EXISTS direct CHAR(1) DEFAULT ('N') NOT NULL CONSTRAINT rhn_act_apply_states_direct_ck CHECK (test in ('Y', 'N'));
