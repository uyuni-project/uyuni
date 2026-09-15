--
-- Copyright (c) 2008 -- 2011 Red Hat, Inc.
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


CREATE TABLE rhnServerNetInterface
(
    id         NUMERIC NOT NULL
                   CONSTRAINT rhn_srv_net_iface_id_pk PRIMARY KEY
                       ,
    server_id  NUMERIC NOT NULL
                   CONSTRAINT rhn_srv_net_iface_sid_fk
                       REFERENCES rhnServer (id),
    name       VARCHAR(32) NOT NULL,
    hw_addr    VARCHAR(96),
    module     VARCHAR(128),
    is_primary VARCHAR(1),
    created    TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL,
    modified   TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_srv_net_iface_sid_name_idx
    ON rhnServerNetInterface (server_id, name)
    ;

CREATE INDEX rhn_srv_net_iface_hw_addr_idx
    ON rhnServerNetInterface (hw_addr)
    ;

ALTER TABLE rhnServerNetInterface
    ADD CONSTRAINT rhn_srv_net_iface_sid_name_uq UNIQUE (server_id, name);

CREATE SEQUENCE rhn_srv_net_iface_id_seq;
