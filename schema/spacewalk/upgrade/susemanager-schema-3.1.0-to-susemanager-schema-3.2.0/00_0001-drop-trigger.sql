-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

drop trigger if exists rhn_virtinst_del_trig on rhnVirtualInstance;
drop function if exists rhn_virtinst_del_trig_fun();
