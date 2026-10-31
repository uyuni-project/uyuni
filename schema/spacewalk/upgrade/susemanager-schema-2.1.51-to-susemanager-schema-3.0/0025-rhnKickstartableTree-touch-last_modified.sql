-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- Update last_modified on every kickstart tree.
-- This will force a re-sync to cobbler the next time
-- something on cobbler changes or taskomatic is restarted.
-- The re-sync will correct the cobbler arch.
update rhnKickstartableTree set last_modified = current_timestamp;
