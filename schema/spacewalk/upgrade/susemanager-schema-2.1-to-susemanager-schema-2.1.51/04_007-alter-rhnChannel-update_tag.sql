-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnChannel
    ALTER COLUMN update_tag TYPE varchar(128);

