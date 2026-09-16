-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO suseInternalState (id, name, label)
  SELECT 13, 'reboot', 'Reboot system'
   WHERE NOT EXISTS (
	SELECT 1 FROM suseInternalState
         WHERE id = 13
   );

INSERT INTO suseInternalState (id, name, label)
  SELECT 14, 'rebootifneeded', 'Reboot system if needed'
   WHERE NOT EXISTS (
	SELECT 1 FROM suseInternalState
         WHERE id = 14
   );

