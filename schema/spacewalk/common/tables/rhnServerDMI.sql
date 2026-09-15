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


CREATE TABLE rhnServerDMI
(
    id            NUMERIC NOT NULL
                      CONSTRAINT rhn_server_dmi_pk PRIMARY KEY
                      ,
    server_id     NUMERIC NOT NULL
                      CONSTRAINT rhn_server_dmi_sid_fk
                          REFERENCES rhnServer (id),
    vendor        VARCHAR(256),
    system        VARCHAR(256),
    product       VARCHAR(256),
    bios_vendor   VARCHAR(256),
    bios_version  VARCHAR(256),
    bios_release  VARCHAR(256),
    asset         VARCHAR(256),
    board         VARCHAR(256),
    created       TIMESTAMPTZ
                      DEFAULT (current_timestamp) NOT NULL,
    modified      TIMESTAMPTZ
                      DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_server_dmi_sid_uq
    ON rhnServerDMI (server_id)
    
    ;

CREATE SEQUENCE rhn_server_dmi_id_seq;

