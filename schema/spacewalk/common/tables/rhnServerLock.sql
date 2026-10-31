--
-- Copyright (c) 2008--2012 Red Hat, Inc.
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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnServerLock
(
    server_id  NUMERIC NOT NULL
                   CONSTRAINT rhn_server_lock_sid_fk
                       REFERENCES rhnServer (id),
    locker_id  NUMERIC
                   CONSTRAINT rhn_server_lock_lid_fk
                       REFERENCES web_contact (id)
                       ON DELETE SET NULL,
    reason     VARCHAR(4000),
    created    TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_server_lock_sid_unq
    ON rhnServerLock (server_id)
    ;

CREATE INDEX rhn_server_lock_lid_unq
    ON rhnServerLock (locker_id)
    ;

