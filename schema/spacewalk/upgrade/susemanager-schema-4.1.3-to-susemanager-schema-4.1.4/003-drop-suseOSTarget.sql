-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

DROP TABLE IF EXISTS suseOSTarget;

DROP SEQUENCE IF EXISTS suse_ostarget_id_seq;

DROP FUNCTION IF EXISTS suse_ostarget_mod_trig_fun();
