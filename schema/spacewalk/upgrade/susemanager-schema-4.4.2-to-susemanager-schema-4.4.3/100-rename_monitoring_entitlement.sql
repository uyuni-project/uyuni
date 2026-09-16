-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

--------------------------------------------------------------------------------
-- rhnServerGroupType ----------------------------------------------------------
--------------------------------------------------------------------------------
UPDATE rhnServerGroupType
SET name = 'Monitored Host'
WHERE label = 'monitoring_entitled';


--------------------------------------------------------------------------------
-- existing server groups update -----------------------------------------------
--------------------------------------------------------------------------------
UPDATE rhnServerGroup sg
SET
    name = sgt.name,
    description = sgt.name
FROM rhnServerGroupType sgt
WHERE
    sg.group_type = sgt.id AND
    sgt.label = 'monitoring_entitled'
;
