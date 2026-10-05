-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- rename the column
ALTER TABLE susePinnedSubscription RENAME COLUMN server_id to system_id;

-- drop the fk constraint
ALTER TABLE susePinnedSubscription DROP CONSTRAINT suse_pinsub_sid_fk;
