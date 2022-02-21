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


CREATE TABLE rhnPackageCapability
(
    id        NUMERIC NOT NULL
                  CONSTRAINT rhn_pkg_capability_id_pk PRIMARY KEY
                  ,
    name      TEXT NOT NULL,
    version   VARCHAR(512),
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
)

;

-- unique index definitions has been moved to
-- {oracle,postgres}/tables/rhnPackageCapability_index.sql

CREATE SEQUENCE rhn_pkg_capability_id_seq;

