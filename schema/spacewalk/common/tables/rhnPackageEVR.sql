--
-- Copyright (c) 2008 Red Hat, Inc.
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


CREATE TABLE rhnPackageEVR
(
    id       NUMERIC NOT NULL
                 CONSTRAINT rhn_pe_id_pk PRIMARY KEY,
    epoch    VARCHAR(16),
    version  VARCHAR(512) NOT NULL,
    release  VARCHAR(512) NOT NULL,
    evr      EVR_T NOT NULL,
    type     varchar(10) generated always as ((evr).type) stored
)

;

-- unique index definitions has been moved to
-- {oracle,postgres}/tables/rhnPackageEVR_index.sql

CREATE SEQUENCE rhn_pkg_evr_seq;

