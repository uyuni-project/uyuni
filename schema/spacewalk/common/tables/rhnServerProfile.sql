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


CREATE TABLE rhnServerProfile
(
    id               NUMERIC NOT NULL,
    org_id           NUMERIC NOT NULL
                         CONSTRAINT rhn_server_profile_oid_fk
                             REFERENCES web_customer (id)
                             ON DELETE CASCADE,
    base_channel     NUMERIC NOT NULL
                         CONSTRAINT rhn_server_profile_bcid_fk
                             REFERENCES rhnChannel (id),
    name             VARCHAR(128),
    description      VARCHAR(256),
    info             VARCHAR(128),
    profile_type_id  NUMERIC NOT NULL
                         CONSTRAINT rhn_server_profile_ptype_fk
                             REFERENCES rhnServerProfileType (id),
    created          TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL,
    modified         TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_server_profile_noid_uq
    ON rhnServerProfile (org_id, name)
    ;

CREATE INDEX rhn_sprofile_id_oid_bc_idx
    ON rhnServerProfile (id, org_id, base_channel)
    ;

CREATE INDEX rhn_server_profile_bc_idx
    ON rhnServerProfile (base_channel)
    
    ;

CREATE SEQUENCE rhn_server_profile_id_seq;

ALTER TABLE rhnServerProfile
    ADD CONSTRAINT rhn_server_profile_id_pk PRIMARY KEY (id);

