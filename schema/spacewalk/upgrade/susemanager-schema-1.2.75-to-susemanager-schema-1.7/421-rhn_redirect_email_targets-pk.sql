-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhn_redirect_email_targets disable constraint rhn_rdret_pk;
drop index rhn_rdret_pk;
alter table rhn_redirect_email_targets enable constraint rhn_rdret_pk;
