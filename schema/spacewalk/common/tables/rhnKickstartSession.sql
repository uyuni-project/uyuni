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


CREATE TABLE rhnKickstartSession
(
    id                   NUMERIC NOT NULL
                             CONSTRAINT rhn_ks_session_id_pk PRIMARY KEY
                             ,
    kickstart_id         NUMERIC
                             CONSTRAINT rhn_ks_session_ksid_fk
                                 REFERENCES rhnKSData (id)
                                 ON DELETE CASCADE,
    kickstart_mode       VARCHAR(32),
    kstree_id            NUMERIC
                             CONSTRAINT rhn_ks_session_kstid_fk
                                 REFERENCES rhnKickstartableTree (id)
                                 ON DELETE SET NULL,
    org_id               NUMERIC NOT NULL
                             CONSTRAINT rhn_ks_session_oid_fk
                                 REFERENCES web_customer (id)
                                 ON DELETE CASCADE,
    scheduler            NUMERIC
                             CONSTRAINT rhn_ks_session_sched_fk
                                 REFERENCES web_contact (id)
                                 ON DELETE SET NULL,
    old_server_id        NUMERIC
                             CONSTRAINT rhn_ks_session_osid_fk
                                 REFERENCES rhnServer (id),
    new_server_id        NUMERIC
                             CONSTRAINT rhn_ks_session_nsid_fk
                                 REFERENCES rhnServer (id),
    host_server_id       NUMERIC
                             CONSTRAINT rhn_ks_session_hsid_fk
                                 REFERENCES rhnServer (id)
                                 ON DELETE CASCADE,
    action_id            NUMERIC
                             CONSTRAINT rhn_ks_session_aid_fk
                                 REFERENCES rhnAction (id)
                                 ON DELETE SET NULL,
    state_id             NUMERIC NOT NULL
                             CONSTRAINT rhn_ks_session_ksssid_fk
                                 REFERENCES rhnKickstartSessionState (id),
    server_profile_id    NUMERIC
                             CONSTRAINT rhn_ks_session_spid_fk
                                 REFERENCES rhnServerProfile (id)
                                 ON DELETE SET NULL,
    last_action          TIMESTAMPTZ
                             DEFAULT (current_timestamp) NOT NULL,
    package_fetch_count  NUMERIC
                             DEFAULT (0) NOT NULL,
    last_file_request    VARCHAR(2048),
    system_rhn_host      VARCHAR(256),
    kickstart_from_host  VARCHAR(256),
    deploy_configs       CHAR(1)
                             DEFAULT ('N') NOT NULL,
    virtualization_type  NUMERIC NOT NULL
                             CONSTRAINT rhn_kss_kvt_fk
                                 REFERENCES rhnKickstartVirtualizationType (id)
                                 ON DELETE SET NULL,
    client_ip            VARCHAR(15),
    created              TIMESTAMPTZ
                             DEFAULT (current_timestamp) NOT NULL,
    modified             TIMESTAMPTZ
                             DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_ks_session_oid_idx
    ON rhnKickstartSession (org_id)
    ;

CREATE INDEX rhn_ks_session_osid_aid_idx
    ON rhnKickstartSession (old_server_id, action_id)
    ;

CREATE INDEX rhn_ks_session_nsid_idx
    ON rhnKickstartSession (new_server_id)
    ;

CREATE INDEX rhn_ks_session_hsid_idx
    ON rhnKickstartSession (host_server_id)
    ;

CREATE SEQUENCE rhn_ks_session_id_seq;

