-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

UPDATE rhnTaskoSchedule SET cron_expr = '0 * * * * ?' WHERE job_label = 'errata-cache-default' AND active_till IS NULL AND cron_expr = '0 0/10 * * * ?';
