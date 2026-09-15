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


CREATE TABLE rhnPushClient
(
    id                 NUMERIC NOT NULL
                           CONSTRAINT rhn_pclient_id_pk PRIMARY KEY
                           ,
    name               VARCHAR(64) NOT NULL,
    server_id          NUMERIC NOT NULL,
    jabber_id          VARCHAR(128),
    shared_key         VARCHAR(64) NOT NULL,
    state_id           NUMERIC NOT NULL
                           REFERENCES rhnPushClientState (id),
    next_action_time   TIMESTAMPTZ,
    last_message_time  TIMESTAMPTZ,
    last_ping_time     TIMESTAMPTZ,
    created            TIMESTAMPTZ
                           DEFAULT (current_timestamp) NOT NULL,
    modified           TIMESTAMPTZ
                           DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_pclient_name_uq
    ON rhnPushClient (name)
    ;

CREATE UNIQUE INDEX rhn_pclient_sid_uq
    ON rhnPushClient (server_id)
    ;

CREATE SEQUENCE rhn_pclient_id_seq;

