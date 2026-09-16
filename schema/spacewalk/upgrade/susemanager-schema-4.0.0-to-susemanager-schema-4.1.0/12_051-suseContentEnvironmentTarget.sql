-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseContentEnvironmentTarget ADD COLUMN IF NOT EXISTS built_time TIMESTAMPTZ;
