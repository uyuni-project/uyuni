-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE web_contact_log ALTER log_id TYPE numeric;
ALTER TABLE rhnServer_log ALTER log_id TYPE numeric;
ALTER TABLE rhnServerGroup_log ALTER log_id TYPE numeric;
