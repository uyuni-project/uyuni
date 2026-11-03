-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhnksdata add update_type VARCHAR(7) default ('none') not null;
alter table rhnksdata add constraint rhn_ks_update_type check (update_type in ('all', 'red_hat', 'none'));
