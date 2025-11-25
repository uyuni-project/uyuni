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
CREATE TABLE rhnActionInventory
(
    id                  NUMERIC NOT NULL
                            CONSTRAINT rhn_action_inventory_id_pk
                                PRIMARY KEY,
    action_id           NUMERIC NOT NULL
                            CONSTRAINT rhn_action_inventory_aid_fk
                                REFERENCES rhnAction (id)
                                ON DELETE CASCADE,
    inventory_path      VARCHAR(1024),
    created             TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL
)
;

CREATE UNIQUE INDEX rhn_act_inventory_aid_uq
    ON rhnActionInventory (action_id);

CREATE SEQUENCE rhn_act_inventory_id_seq;

