-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnactivationkey ADD bootstrap CHAR(1) DEFAULT ('N') NOT NULL;
ALTER TABLE rhnactivationkey ADD CONSTRAINT rhn_act_key_bootstrap_ck CHECK (bootstrap in ('Y', 'N'));


