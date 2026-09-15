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


CREATE TABLE rhnKickstartIPRange
(
    kickstart_id  NUMERIC NOT NULL
                      CONSTRAINT rhn_ksip_ksid_fk
                          REFERENCES rhnKSData (id)
                          ON DELETE CASCADE,
    org_id        NUMERIC NOT NULL
                      CONSTRAINT rhn_ksip_oid_fk
                          REFERENCES web_customer (id)
                          ON DELETE CASCADE,
    min           NUMERIC NOT NULL,
    max           NUMERIC NOT NULL,
    created       TIMESTAMPTZ
                      DEFAULT (current_timestamp) NOT NULL,
    modified      TIMESTAMPTZ
                      DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_ksip_kickstart_id_idx
    ON rhnKickstartIPRange (kickstart_id)
    ;

CREATE INDEX rhn_ksip_org_id_idx
    ON rhnKickstartIPRange (org_id)
    ;

ALTER TABLE rhnKickstartIPRange
    ADD CONSTRAINT rhn_ksip_oid_min_max_uq UNIQUE (org_id, min, max);

