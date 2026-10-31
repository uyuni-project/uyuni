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


CREATE TABLE rhnDevice
(
    id           NUMERIC NOT NULL
                     CONSTRAINT rhn_device_id_pk PRIMARY KEY
                     ,
    server_id    NUMERIC NOT NULL
                     CONSTRAINT rhn_device_sid_fk
                         REFERENCES rhnServer (id)
                         ON DELETE CASCADE,
    class        VARCHAR(16),
    bus          VARCHAR(16),
    detached     NUMERIC,
    device       VARCHAR(256),
    driver       VARCHAR(256),
    description  VARCHAR(256),
    pcitype      NUMERIC
                     DEFAULT (-1),
    prop1        VARCHAR(256),
    prop2        VARCHAR(256),
    prop3        VARCHAR(256),
    prop4        VARCHAR(256),
    created      TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    modified     TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_device_server_id_idx
    ON rhnDevice (server_id)
    
    ;

CREATE SEQUENCE rhn_hw_dev_id_seq;

