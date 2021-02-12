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


CREATE TABLE rhnVirtualInstanceInfo
(
    name           VARCHAR(128),
    instance_id    NUMERIC NOT NULL
                       CONSTRAINT rhn_vii_viid_fk
                           REFERENCES rhnVirtualInstance (id)
                           ON DELETE CASCADE,
    instance_type  NUMERIC NOT NULL
                       CONSTRAINT rhn_vii_it_fk
                           REFERENCES rhnVirtualInstanceType (id),
    memory_size    NUMERIC,
    vcpus          NUMERIC,
    state          NUMERIC NOT NULL
                       CONSTRAINT rhn_vii_state_fk
                           REFERENCES rhnVirtualInstanceState (id),
    created        TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_vii_viid_uq
    ON rhnVirtualInstanceInfo (instance_id)
    ;

CREATE SEQUENCE rhn_vii_id_seq;

