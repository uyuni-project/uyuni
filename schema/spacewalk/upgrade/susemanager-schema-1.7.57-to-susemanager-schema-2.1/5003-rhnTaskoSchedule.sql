-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnTaskoSchedule ALTER cron_expr TYPE VARCHAR(120);
