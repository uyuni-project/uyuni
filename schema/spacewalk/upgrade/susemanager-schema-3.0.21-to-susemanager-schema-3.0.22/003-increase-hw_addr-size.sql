-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnServerNetInterface ALTER COLUMN hw_addr TYPE VARCHAR(96);
