-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseProductChannel
  DROP constraint spc_pid_fk;

ALTER TABLE suseProductChannel
  ADD constraint spc_pid_fk
    foreign key (product_id)
    references suseProducts (id)
    ON DELETE CASCADE;

ALTER TABLE suseProductChannel
  DROP constraint spc_rhn_cid_fk;

ALTER TABLE suseProductChannel
  ADD constraint spc_rhn_cid_fk
    foreign key (channel_id)
    references rhnChannel (id)
    ON DELETE SET NULL;

