-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

DELETE FROM 
  rhnServerNetwork
    WHERE id IN
    (
        SELECT DISTINCT(a.id)
        FROM rhnServerNetwork a, rhnServerNetwork b, suseMinionInfo c
        WHERE
            a.server_id = b.server_id
            AND a.id < b.id
            AND a.server_id = c.server_id
    )
;
