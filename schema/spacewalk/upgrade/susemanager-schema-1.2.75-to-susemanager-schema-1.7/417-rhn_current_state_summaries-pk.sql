-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhn_current_state_summaries disable constraint rhn_current_state_summaries_pk;
drop index rhn_current_state_summaries_pk;
alter table rhn_current_state_summaries enable constraint rhn_current_state_summaries_pk;
