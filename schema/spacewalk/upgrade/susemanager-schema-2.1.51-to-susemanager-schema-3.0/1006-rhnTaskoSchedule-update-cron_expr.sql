-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

update rhnTaskoSchedule set cron_expr = '0 * * * * ?' where job_label = 'errata-cache-default';
