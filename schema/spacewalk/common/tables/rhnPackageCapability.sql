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
    id        NUMBER NOT NULL
                  CONSTRAINT rhn_pkg_capability_id_pk PRIMARY KEY
                  USING INDEX TABLESPACE [[4m_tbs]],
    name      VARCHAR2(4000) NOT NULL,
    version   VARCHAR2(512),
    created   timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL,
    modified  timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

-- unique index definitions has been moved to
-- {oracle,postgres}/tables/rhnPackageCapability_index.sql

CREATE SEQUENCE rhn_pkg_capability_id_seq;

