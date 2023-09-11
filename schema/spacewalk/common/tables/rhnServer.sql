--
-- Copyright (c) 2008--2014 Red Hat, Inc.
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


CREATE TABLE rhnServer
(
    id                  NUMERIC NOT NULL
                            CONSTRAINT rhn_server_id_pk PRIMARY KEY
                            ,
    org_id              NUMERIC NOT NULL
                            CONSTRAINT rhn_server_oid_fk
                                REFERENCES web_customer (id)
                                ON DELETE CASCADE,
    digital_server_id   VARCHAR(1024) NOT NULL,
    server_arch_id      NUMERIC NOT NULL
                            CONSTRAINT rhn_server_said_fk
                                REFERENCES rhnServerArch (id),
    os                  VARCHAR(64) NOT NULL,
    release             VARCHAR(64) NOT NULL,
    name                VARCHAR(128),
    description         VARCHAR(256),
    info                VARCHAR(128),
    secret              VARCHAR(64) NOT NULL,
    creator_id          NUMERIC
                            CONSTRAINT rhn_server_creator_fk
                                REFERENCES web_contact (id)
                                ON DELETE SET NULL,
    auto_update         CHAR(1)
                            DEFAULT ('N') NOT NULL
                            CONSTRAINT rhn_server_update_ck
                                CHECK (auto_update in ('Y', 'N')),
    contact_method_id   NUMERIC
                            DEFAULT (0) NOT NULL
                            CONSTRAINT rhn_server_cmid_fk
                                REFERENCES suseServerContactMethod (id),
    running_kernel      VARCHAR(64),
    last_boot           NUMERIC
                            DEFAULT (0) NOT NULL,
    provision_state_id  NUMERIC
                            CONSTRAINT rhn_server_psid_fk
                                REFERENCES rhnProvisionState (id),
    channels_changed    TIMESTAMPTZ,
    cobbler_id          VARCHAR(64),
    machine_id          VARCHAR(256),
    hostname            VARCHAR(128),
    payg                CHAR(1) DEFAULT ('N') NOT NULL,
    maintenance_schedule_id NUMERIC
                            CONSTRAINT rhn_server_mtsched_id_fk
                                REFERENCES suseMaintenanceSchedule (id)
                                ON DELETE SET NULL,
    created             TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    cpe                 VARCHAR(64)
)

;

CREATE UNIQUE INDEX rhn_server_dsid_uq
    ON rhnServer (digital_server_id)
    ;

CREATE INDEX rhn_server_oid_id_idx
    ON rhnServer (org_id, id)
    
    ;

CREATE INDEX rhn_server_created_id_idx
    ON rhnServer (created, id)
    
    ;

CREATE INDEX rhn_server_creator_idx
    ON rhnServer (creator_id)
    
    ;

CREATE INDEX rhn_server_hostname_idx
    ON rhnServer (hostname)
    
    ;

CREATE SEQUENCE rhn_server_id_seq START WITH 1000010000;
