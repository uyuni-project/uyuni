-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO rhnTaskQueue (org_id, task_name, task_data)
SELECT id, 'upgrade_satellite_virthost_pillar_refresh', 0
FROM web_customer
WHERE id = 1;
