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


CREATE TABLE rhnVirtualInstance
(
    id                 NUMERIC NOT NULL
                           CONSTRAINT rhn_vi_id_pk PRIMARY KEY
                           ,
    host_system_id     NUMERIC
                           CONSTRAINT rhn_vi_hsi_fk
                               REFERENCES rhnServer (id),
    virtual_system_id  NUMERIC
                           CONSTRAINT rhn_vi_vsi_fk
                               REFERENCES rhnServer (id),
    uuid               VARCHAR(128),
    confirmed          NUMERIC(1)
                           DEFAULT (1) NOT NULL,
    created            TIMESTAMPTZ
                           DEFAULT (current_timestamp) NOT NULL,
    modified           TIMESTAMPTZ
                           DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_vi_hsid_vsid_idx
    ON rhnVirtualInstance (host_system_id, virtual_system_id)
    ;

CREATE INDEX rhn_vi_vsid_idx
    ON rhnVirtualInstance (virtual_system_id)
    ;

CREATE INDEX rhn_vi_uuid_idx
    ON rhnVirtualInstance (uuid)
    ;

CREATE SEQUENCE rhn_vi_id_seq;

