--
-- Copyright (c) 2025 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--
CREATE TABLE IF NOT EXISTS suseAnsibleInventoryServers(

    inventory_id NUMERIC NOT NULL
        CONSTRAINT suse_ansible_inventory_id_fk
        REFERENCES suseAnsiblePath(id)
        ON DELETE CASCADE,

    server_id NUMERIC NOT NULL
        CONSTRAINT suse_ansible_inventory_sid_fk
        REFERENCES rhnServer(id)
        ON DELETE CASCADE,

    created     TIMESTAMPTZ
        DEFAULT (current_timestamp) NOT NULL,

    modified    TIMESTAMPTZ
        DEFAULT (current_timestamp) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS suse_ansible_inventory_server_uq
    ON suseAnsibleInventoryServers(inventory_id, server_id);
