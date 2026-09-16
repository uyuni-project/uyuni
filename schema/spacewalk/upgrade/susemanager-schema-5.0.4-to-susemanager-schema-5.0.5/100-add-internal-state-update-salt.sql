-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO suseInternalState (id, name, label)
  SELECT 12, 'update-salt', 'Update Salt'
   WHERE NOT EXISTS (
	SELECT 1 FROM suseInternalState
         WHERE id = 12
   );
