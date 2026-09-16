-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

insert into rhnActionType
SELECT 526, 'supportdata.get', 'Get supportdata from a system', 'N', 'N', 'N'
WHERE NOT EXISTS (SELECT 1
                  FROM rhnActionType
                  WHERE id = 526);
