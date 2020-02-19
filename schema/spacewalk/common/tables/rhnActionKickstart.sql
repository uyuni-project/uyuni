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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnActionKickstart
(
    id                  NUMERIC NOT NULL,
    action_id           NUMERIC NOT NULL
                            CONSTRAINT rhn_actionks_aid_fk
                                REFERENCES rhnAction (id)
                                ON DELETE CASCADE,
    append_string       VARCHAR(1024),
    kickstart_host      VARCHAR(256),
    static_device       VARCHAR(32),
    cobbler_system_name VARCHAR(256),
    upgrade             CHAR(1) DEFAULT ('N') NOT NULL
                            CONSTRAINT rhn_actionks_up_ck
                                CHECK (upgrade in ('Y', 'N')),
    created             TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_actionks_aid_uq
    ON rhnActionKickstart (action_id)
    ;

CREATE INDEX rhn_actionks_id_idx
    ON rhnActionKickstart (id)
    ;

CREATE SEQUENCE rhn_actionks_id_seq;

ALTER TABLE rhnActionKickstart
    ADD CONSTRAINT rhn_actionks_id_pk PRIMARY KEY (id);

