-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnChannel
    DROP CONSTRAINT IF EXISTS rhn_channel_ru_ck;

ALTER TABLE rhnChannel
    DROP COLUMN IF EXISTS receiving_updates;

