-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhn_time_zone_names disable constraint rhn_time_zone_names_uq;
drop index rhn_time_zone_names_uq;
alter table rhn_time_zone_names enable constraint rhn_time_zone_names_uq;
