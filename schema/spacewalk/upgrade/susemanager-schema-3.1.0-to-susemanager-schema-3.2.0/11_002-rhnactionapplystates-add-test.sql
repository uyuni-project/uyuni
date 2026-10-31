-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnActionApplyStates ADD test CHAR(1) DEFAULT ('N') NOT NULL CONSTRAINT rhn_act_apply_states_test_ck CHECK (test in ('Y', 'N'));

