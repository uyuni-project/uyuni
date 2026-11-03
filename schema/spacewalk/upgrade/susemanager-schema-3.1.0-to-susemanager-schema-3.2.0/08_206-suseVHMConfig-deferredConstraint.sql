-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

drop index suse_vhmc_id_para_uq;
alter table suseVHMConfig add constraint suse_vhmc_id_para_uq unique (virtual_host_manager_id, parameter) DEFERRABLE;

