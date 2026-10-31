-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO rhnOrgAdminManagement (org_id) (SELECT id FROM web_customer);
