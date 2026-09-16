-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnContentSource
    ALTER COLUMN source_url TYPE varchar(2048);
