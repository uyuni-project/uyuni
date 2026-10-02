-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

select .clear_log_id();

UPDATE rhnServerGroup
   SET max_members = 0
 WHERE max_members IS NULL
   AND group_type IS NOT NULL;
