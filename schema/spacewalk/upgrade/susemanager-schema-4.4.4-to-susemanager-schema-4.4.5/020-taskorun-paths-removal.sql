-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnTaskoRun DROP COLUMN IF EXISTS std_output_path;
ALTER TABLE rhnTaskoRun DROP COLUMN IF EXISTS std_error_path;
