-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhn_redirect_group_targets disable constraint rhn_rdrgt_pk;
drop index rhn_rdrgt_pk;
alter table rhn_redirect_group_targets enable constraint rhn_rdrgt_pk;
