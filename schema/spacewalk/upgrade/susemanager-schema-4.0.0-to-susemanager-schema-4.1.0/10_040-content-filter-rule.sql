-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseContentFilter ADD COLUMN IF NOT EXISTS rule VARCHAR(16);

