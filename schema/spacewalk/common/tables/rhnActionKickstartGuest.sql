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


CREATE TABLE rhnActionKickstartGuest
(
    id                   NUMERIC NOT NULL,
    action_id            NUMERIC NOT NULL
                             CONSTRAINT rhn_actionks_xenguest_aid_fk
                                 REFERENCES rhnAction (id)
                                 ON DELETE CASCADE,
    append_string        VARCHAR(1024),
    ks_session_id        NUMERIC
                             CONSTRAINT rhn_actionks_xenguest_ksid_fk
                                 REFERENCES rhnKickstartSession (id)
                                 ON DELETE CASCADE,
    guest_name           VARCHAR(256),
    mem_kb               NUMERIC,
    vcpus                NUMERIC,
    disk_gb              NUMERIC,
    cobbler_system_name  VARCHAR(256),
    disk_path            VARCHAR(256),
    virt_bridge          VARCHAR(256),
    kickstart_host       VARCHAR(256),
    created              TIMESTAMPTZ
                             DEFAULT (current_timestamp) NOT NULL,
    modified             TIMESTAMPTZ
                             DEFAULT (current_timestamp) NOT NULL,
    mac_address          VARCHAR(17)
)

;

CREATE UNIQUE INDEX rhn_actionks_xenguest_aid_uq
    ON rhnActionKickstartGuest (action_id)
    ;

CREATE INDEX rhn_actionks_xenguest_id_idx
    ON rhnActionKickstartGuest (id)
    ;

CREATE SEQUENCE rhn_actionks_xenguest_id_seq;

ALTER TABLE rhnActionKickstartGuest
    ADD CONSTRAINT rhn_actionks_xenguest_id_pk PRIMARY KEY (id);

