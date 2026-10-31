-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnCpu
    ADD COLUMN IF NOT EXISTS nrcore   NUMERIC DEFAULT (1),
    ADD COLUMN IF NOT EXISTS nrthread NUMERIC DEFAULT (1),
    ALTER COLUMN nrsocket SET DEFAULT 1;
