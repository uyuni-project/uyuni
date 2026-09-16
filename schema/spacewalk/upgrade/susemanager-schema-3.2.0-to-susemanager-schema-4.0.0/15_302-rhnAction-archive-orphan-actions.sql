-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- Archive actions which have no servers assigned
UPDATE rhnAction SET archived = 1 WHERE id IN (
    SELECT id FROM rhnUserActionOverview
    WHERE user_id IS NULL
);
