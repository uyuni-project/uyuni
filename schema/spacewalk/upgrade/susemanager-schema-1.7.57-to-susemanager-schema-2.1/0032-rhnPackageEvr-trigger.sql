-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

create trigger
rhn_pack_evr_no_updel_trig
before update or delete on rhnPackageEvr
execute procedure no_operation_trig_fun();

