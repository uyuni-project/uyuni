-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- Drop old stuff
delete from rhnContentSourceSsl;
drop table rhnContentSourceSsl;
drop function rhn_csssl_ins_trig_fun();
drop function rhn_cont_source_ssl_mod_trig_fun();
drop sequence rhn_contentsourcessl_seq;
