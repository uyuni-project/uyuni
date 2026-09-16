-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO rhnServerNetAddress4
    (interface_id, address, netmask, broadcast, created)
    SELECT id, ip_addr, netmask, broadcast, created
    FROM rhnServerNetInterface;

COMMIT;
